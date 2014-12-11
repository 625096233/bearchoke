/*
 * Copyright 2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.bearchoke.platform.platform.base.config;

import com.mongodb.Mongo;
import org.axonframework.commandhandling.CommandBus;
import org.axonframework.commandhandling.CommandDispatchInterceptor;
import org.axonframework.commandhandling.SimpleCommandBus;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.commandhandling.gateway.CommandGatewayFactoryBean;
import org.axonframework.commandhandling.interceptors.BeanValidationInterceptor;
import org.axonframework.eventsourcing.Snapshotter;
import org.axonframework.eventsourcing.SpringAggregateSnapshotter;
import org.axonframework.eventstore.mongo.DefaultMongoTemplate;
import org.axonframework.eventstore.mongo.MongoEventStore;
import org.axonframework.eventstore.mongo.MongoTemplate;
import org.axonframework.saga.SagaRepository;
import org.axonframework.saga.repository.mongo.MongoSagaRepository;
import org.axonframework.saga.spring.SpringResourceInjector;
import org.axonframework.springmessaging.eventbus.SpringMessagingEventBus;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.messaging.SubscribableChannel;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Bjorn Harvold
 * Date: 7/19/14
 * Time: 10:36 PM
 * Responsibility:
 */
@Configuration
public class CQRSConfig {

    @Inject
    @Qualifier("taskExecutor")
    private TaskExecutor taskExecutor;

    @Inject
    private Mongo mongo;

    @Inject
    @Qualifier("webSocketInputChannel")
    private SubscribableChannel webSocketInputChannel;

    @Bean(name = "commandBus")
    public CommandBus commandBus() {
        SimpleCommandBus commandBus = new SimpleCommandBus();

        List<CommandDispatchInterceptor> interceptors = new ArrayList<>(1);
        interceptors.add(new BeanValidationInterceptor());
        commandBus.setDispatchInterceptors(interceptors);

        return commandBus;
    }

    @Bean(name = "commandGateway")
    public CommandGateway commandGateway() {
        CommandGateway commandGateway = null;
        CommandGatewayFactoryBean factory = new CommandGatewayFactoryBean();
        factory.setCommandBus(commandBus());
//        factory.setGatewayInterface();


        try {
            factory.afterPropertiesSet();
            commandGateway = (CommandGateway) factory.getObject();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return commandGateway;
    }

    @Bean(name = "eventBus")
    public SpringMessagingEventBus eventBus() {
        SpringMessagingEventBus eventBus = new SpringMessagingEventBus();
        eventBus.setChannel(webSocketInputChannel);

        return eventBus;
    }

    @Bean
    public MongoTemplate mongoTemplate() {
        return new DefaultMongoTemplate(mongo);
    }

    @Bean
    public org.axonframework.saga.repository.mongo.MongoTemplate mongoSagaTemplate() {
        return new org.axonframework.saga.repository.mongo.DefaultMongoTemplate(mongo);
    }

    @Bean(name = "eventStore")
    public MongoEventStore eventStore() {
        MongoEventStore eventStore = new MongoEventStore(mongoTemplate());

        return eventStore;
    }

    @Bean(name = "sagaRepository")
    public SagaRepository sagaRepository() {
        MongoSagaRepository sagaRepository = new MongoSagaRepository(mongoSagaTemplate());
        sagaRepository.setResourceInjector(new SpringResourceInjector());

        return sagaRepository;
    }

    @Bean(name = "snapshotter")
    public Snapshotter snapshotter() {
        SpringAggregateSnapshotter sas = new SpringAggregateSnapshotter();
        sas.setEventStore(eventStore());
        sas.setExecutor(taskExecutor);

        return sas;
    }

}
