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

package com.bearchoke.platform.api.todo.dto;

import com.bearchoke.platform.api.todo.enums.ToDoAction;
import com.bearchoke.platform.api.todo.event.ToDoItemCompletedEvent;
import com.bearchoke.platform.api.todo.event.ToDoItemCreatedEvent;
import com.bearchoke.platform.api.todo.event.ToDoItemRemovedEvent;
import lombok.Data;

/**
 * Created by Bjorn Harvold
 * Date: 10/11/14
 * Time: 5:28 PM
 * Responsibility:
 */
@Data
public class ToDoDto {
    private ToDoAction action;
    private String id;
    private String description;

    public ToDoDto() {

    }

    public ToDoDto(ToDoItemCreatedEvent event) {
        this.action = event.getAction();
        this.id = event.getTodoId();
        this.description = event.getDescription();
    }

    public ToDoDto(ToDoItemCompletedEvent event) {
        this.action = event.getAction();
        this.id = event.getTodoId();
    }

    public ToDoDto(ToDoItemRemovedEvent event) {
        this.action = event.getAction();
        this.id = event.getTodoId();
    }
}