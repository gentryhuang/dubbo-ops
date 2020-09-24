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
package com.alibaba.dubboadmin.governance.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.URL;
import com.alibaba.dubboadmin.governance.service.OverrideService;
import com.alibaba.dubboadmin.governance.sync.util.Pair;
import com.alibaba.dubboadmin.governance.sync.util.SyncUtils;
import com.alibaba.dubboadmin.registry.common.domain.Override;

import org.springframework.stereotype.Component;

/**
 * IbatisOverrideDAO.java
 */
@Component
public class OverrideServiceImpl extends AbstractService implements OverrideService {

    /**
     * 新增配置 - 注册
     *
     * @param override
     */
    @java.lang.Override
    public void saveOverride(Override override) {
        URL url = getUrlFromOverride(override);
        registryService.register(url);
    }

    /**
     * 更新配置 - 删除旧的，重新注册新的
     *
     * @param override
     */
    @java.lang.Override
    public void updateOverride(Override override) {
        Long id = override.getId();
        if (id == null) {
            throw new IllegalStateException("no override id");
        }
        URL oldOverride = findOverrideUrl(id);
        if (oldOverride == null) {
            throw new IllegalStateException("Route was changed!");
        }
        URL newOverride = getUrlFromOverride(override);

        registryService.unregister(oldOverride);
        registryService.register(newOverride);

    }

    /**
     * 删除动态配置 - 从注册中心移除
     *
     * @param id
     */
    @java.lang.Override
    public void deleteOverride(Long id) {
        URL oldOverride = findOverrideUrl(id);
        if (oldOverride == null) {
            throw new IllegalStateException("Route was changed!");
        }
        registryService.unregister(oldOverride);
    }

    /**
     * 启用配置 - 删除旧的，注册新的（enabled = true）
     *
     * @param id
     */
    @java.lang.Override
    public void enableOverride(Long id) {
        if (id == null) {
            throw new IllegalStateException("no override id");
        }

        URL oldOverride = findOverrideUrl(id);
        if (oldOverride == null) {
            throw new IllegalStateException("Override was changed!");
        }

        if (oldOverride.getParameter("enabled", true)) {
            return;
        }

        URL newOverride = oldOverride.addParameter("enabled", true);
        registryService.unregister(oldOverride);
        registryService.register(newOverride);

    }

    /**
     * 禁用配置 - 删除旧的，注册新的（enabled = true）
     *
     * @param id
     */
    @java.lang.Override
    public void disableOverride(Long id) {
        if (id == null) {
            throw new IllegalStateException("no override id");
        }

        URL oldProvider = findOverrideUrl(id);
        if (oldProvider == null) {
            throw new IllegalStateException("Override was changed!");
        }
        if (!oldProvider.getParameter("enabled", true)) {
            return;
        }

        URL newProvider = oldProvider.addParameter("enabled", false);
        registryService.unregister(oldProvider);
        registryService.register(newProvider);

    }

    /**
     * 筛选配置
     *
     * @param service     服务名
     * @param address     地址
     * @param application 应用名
     * @return
     */
    private Map<Long, URL> findOverrideUrl(String service, String address, String application) {
        Map<String, String> filter = new HashMap<String, String>();
        filter.put(Constants.CATEGORY_KEY, Constants.CONFIGURATORS_CATEGORY);
        if (service != null && service.length() > 0) {
            filter.put(SyncUtils.SERVICE_FILTER_KEY, service);
        }
        if (address != null && address.length() > 0) {
            filter.put(SyncUtils.ADDRESS_FILTER_KEY, address);
        }
        if (application != null && application.length() > 0) {
            filter.put(Constants.APPLICATION_KEY, application);
        }
        return SyncUtils.filterFromCategory(getRegistryCache(), filter);
    }

    /**
     * 根据地址查询配置
     *
     * @param address
     * @return
     */
    @java.lang.Override
    public List<Override> findByAddress(String address) {
        return SyncUtils.url2OverrideList(findOverrideUrl(null, address, null));
    }

    /**
     * 根据服务和地址查询配置
     *
     * @param service
     * @param address
     * @return
     */
    @java.lang.Override
    public List<Override> findByServiceAndAddress(String service, String address) {
        return SyncUtils.url2OverrideList(findOverrideUrl(service, address, null));
    }

    /**
     * 根据应用查询配置
     *
     * @param application
     * @return
     */
    @java.lang.Override
    public List<Override> findByApplication(String application) {
        return SyncUtils.url2OverrideList(findOverrideUrl(null, null, application));
    }

    /**
     * 根据服务名查询配置
     *
     * @param service
     * @return
     */
    @java.lang.Override
    public List<Override> findByService(String service) {
        return SyncUtils.url2OverrideList(findOverrideUrl(service, null, null));
    }

    /**
     * 根据服务和应用查询
     *
     * @param service
     * @param application
     * @return
     */
    @java.lang.Override
    public List<Override> findByServiceAndApplication(String service, String application) {
        return SyncUtils.url2OverrideList(findOverrideUrl(service, null, application));
    }

    /**
     * 查询所有配置
     *
     * @return
     */
    @java.lang.Override
    public List<Override> findAll() {
        return SyncUtils.url2OverrideList(findOverrideUrl(null, null, null));
    }

    /**
     * 根据id查询
     *
     * @param id
     * @return
     */
    private Pair<Long, URL> findOverrideUrlPair(Long id) {
        return SyncUtils.filterFromCategory(getRegistryCache(), Constants.CONFIGURATORS_CATEGORY, id);
    }

    @java.lang.Override
    public Override findById(Long id) {
        return SyncUtils.url2Override(findOverrideUrlPair(id));
    }

    /**
     * 获取 Override 的URL
     *
     * @param override
     * @return
     */
    private URL getUrlFromOverride(Override override) {
        return override.toUrl();
        /*Map<String, String> params = ConvertUtil.serviceName2Map(override.getService());
        if(!params.containsKey(Constants.INTERFACE_KEY)) {
            throw new IllegalArgumentException("No interface info");
        }
        if(!params.containsKey(Constants.VERSION_KEY)) {
            throw new IllegalArgumentException("No version info");
        }
        
        boolean enabled = override.isEnabled();
        if(!enabled) {
            params.put("enabled", "false");
        }
        String application = override.getApplication();
        if(!StringUtils.isEmpty(application)) {
            params.put("application", application);
        }
        String address = override.getAddress();
        if(!StringUtils.isEmpty(address)) {
            params.put("address", address);
        }
        
        String overrideAddress = override.getOverrideAddress();
        if(StringUtils.isEmpty(overrideAddress)) {
            overrideAddress = "0.0.0.0";
        }
        params.put(Constants.CATEGORY_KEY, Constants.CONFIGURATORS_CATEGORY);
        
        URL url = new URL("override", overrideAddress, -1, params);
        url = url.addParameterString(override.getParams());
        return url;*/
    }

    URL findOverrideUrl(Long id) {
        return getUrlFromOverride(findById(id));
    }

}
