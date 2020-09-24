/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.dubboadmin.governance.service;

import java.util.List;

import com.alibaba.dubboadmin.registry.common.domain.Consumer;

/**
 * Query service for consumer info
 */
public interface ConsumerService {

    /**
     * 根据服务名获取消费者列表
     *
     * @param serviceName
     * @return
     */
    List<Consumer> findByService(String serviceName);

    /**
     * 根据消费者id获取
     *
     * @param id
     * @return
     */
    Consumer findConsumer(Long id);

    /**
     * 获取所有消费者
     *
     * @return
     */
    List<Consumer> findAll();

    /**
     * 获取所有的消费者地址
     */
    List<String> findAddresses();

    /**
     * 根据应用名获取所有消费者的地址
     *
     * @param application
     * @return
     */
    List<String> findAddressesByApplication(String application);

    /**
     * 根据服务名获取所有消费者地址
     *
     * @param serviceName
     * @return
     */
    List<String> findAddressesByService(String serviceName);

    /**
     * 根据消费者地址获取消费者列表
     *
     * @param consumerAddress
     * @return
     */
    List<Consumer> findByAddress(String consumerAddress);

    /**
     * 根据消费者地址获取服务名
     *
     * @param consumerAddress
     * @return
     */
    List<String> findServicesByAddress(String consumerAddress);

    /**
     * 获取消费者的所有应用名
     *
     * @return
     */
    List<String> findApplications();

    /**
     * 根据服务名获取应用名
     *
     * @param serviceName
     * @return
     */
    List<String> findApplicationsByServiceName(String serviceName);

    /**
     * 根据应用名获取消费者集合
     *
     * @param application
     * @return
     */
    List<Consumer> findByApplication(String application);

    /**
     * 根据应用名获取服务名集合
     *
     * @param application
     * @return
     */
    List<String> findServicesByApplication(String application);

    /**
     * 获取所有服务名
     *
     * @return
     */
    List<String> findServices();

}