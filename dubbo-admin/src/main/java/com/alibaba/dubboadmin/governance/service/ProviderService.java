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

import com.alibaba.dubboadmin.registry.common.domain.Provider;

/**
 * ProviderService
 * 查询类的规则：都是根据过滤属性去缓存中的URL中进行匹配，然后根据需要进行解析
 */
public interface ProviderService {

    /**
     * 创建提供者
     *
     * @param provider
     */
    void create(Provider provider);

    /**
     * 启用提供者
     *
     * @param id
     */
    void enableProvider(Long id);

    /**
     * 禁用提供者
     *
     * @param id
     */
    void disableProvider(Long id);

    /**
     * 倍权
     *
     * @param id
     */
    void doublingProvider(Long id);

    /**
     * 半权
     *
     * @param id
     */
    void halvingProvider(Long id);

    /**
     * 删除
     *
     * @param id
     */
    void deleteStaticProvider(Long id);

    /**
     * 修改
     *
     * @param provider
     */
    void updateProvider(Provider provider);

    /**
     * 根据id查询
     *
     * @param id
     * @return
     */
    Provider findProvider(Long id);

    /**
     * 获取所有提供者的服务名
     *
     * @return
     */
    List<String> findServices();

    /**
     * 获取所有提供者的服务地址
     *
     * @return
     */
    List<String> findAddresses();

    /**
     * 根据应用获取所有提供者的服务地址
     *
     * @param application
     * @return
     */
    List<String> findAddressesByApplication(String application);

    /**
     * 根据具体的服务名获取地址列表
     *
     * @param serviceName
     * @return
     */
    List<String> findAddressesByService(String serviceName);

    /**
     * 根据具体的服务名获取对应的应用名集合
     *
     * @param serviceName
     * @return
     */
    List<String> findApplicationsByServiceName(String serviceName);

    /**
     * 根据服务名获取提供者列表
     *
     * @param serviceName
     * @return
     */
    List<Provider> findByService(String serviceName);

    /**
     * 获取所有的提供者
     *
     * @return
     */
    List<Provider> findAll();

    /**
     * 根据提供者地址获取提供者
     *
     * @param providerAddress
     * @return
     */
    List<Provider> findByAddress(String providerAddress);

    /**
     * 根据提供者地址获取提供者名
     *
     * @param providerAddress
     * @return
     */
    List<String> findServicesByAddress(String providerAddress);

    /**
     * 获取提供者的所有应用名
     *
     * @return
     */
    List<String> findApplications();

    /**
     * 根据应用名获取对应的提供者集合
     *
     * @param application
     * @return
     */
    List<Provider> findByApplication(String application);

    /**
     * 根据应用名获取对应的提供者名称集合
     *
     * @param application
     * @return
     */
    List<String> findServicesByApplication(String application);

    /**
     * 根据服务名获取对应的方法名
     *
     * @param serviceName
     * @return
     */
    List<String> findMethodsByService(String serviceName);

    /**
     * 根据服务名和地址获取提供者
     *
     * @param service
     * @param address
     * @return
     */
    Provider findByServiceAndAddress(String service, String address);

}