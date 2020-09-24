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
import com.alibaba.dubboadmin.governance.service.RouteService;
import com.alibaba.dubboadmin.governance.sync.util.Pair;
import com.alibaba.dubboadmin.governance.sync.util.SyncUtils;
import com.alibaba.dubboadmin.registry.common.domain.Route;

import org.springframework.stereotype.Component;

/**
 * IbatisRouteService
 */
@Component
public class RouteServiceImpl extends AbstractService implements RouteService {

    /**
     * 创建路由
     *
     * @param route
     */
    @Override
    public void createRoute(Route route) {
        registryService.register(route.toUrl());
    }

    /**
     * 修改路由 - 先取消注册旧的再创建新的路由
     *
     * @param route
     */
    @Override
    public void updateRoute(Route route) {
        Long id = route.getId();
        if (id == null) {
            throw new IllegalStateException("no route id");
        }
        URL oldRoute = findRouteUrl(id);
        if (oldRoute == null) {
            throw new IllegalStateException("Route was changed!");
        }

        registryService.unregister(oldRoute);
        registryService.register(route.toUrl());
    }

    /**
     * 删除路由 - 取消注册
     *
     * @param id
     */
    @Override
    public void deleteRoute(Long id) {
        URL oldRoute = findRouteUrl(id);
        if (oldRoute == null) {
            throw new IllegalStateException("Route was changed!");
        }
        registryService.unregister(oldRoute);
    }

    /**
     * 启用路由 - 删除旧的，重新创建新的
     *
     * @param id
     */
    @Override
    public void enableRoute(Long id) {
        if (id == null) {
            throw new IllegalStateException("no route id");
        }

        URL oldRoute = findRouteUrl(id);
        if (oldRoute == null) {
            throw new IllegalStateException("Route was changed!");
        }
        if (oldRoute.getParameter("enabled", true)) {
            return;
        }

        // 取消注册旧的路由
        registryService.unregister(oldRoute);

        // 注册新的路由，enable = true
        URL newRoute = oldRoute.addParameter("enabled", true);
        registryService.register(newRoute);

    }

    /**
     * 禁用路由 - 删除旧的，重新创建新的
     *
     * @param id
     */
    @Override
    public void disableRoute(Long id) {
        if (id == null) {
            throw new IllegalStateException("no route id");
        }

        URL oldRoute = findRouteUrl(id);
        if (oldRoute == null) {
            throw new IllegalStateException("Route was changed!");
        }
        if (!oldRoute.getParameter("enabled", true)) {
            return;
        }

        URL newRoute = oldRoute.addParameter("enabled", false);
        registryService.unregister(oldRoute);
        registryService.register(newRoute);

    }

    /**
     * 获取所有路由
     *
     * @return
     */
    @Override
    public List<Route> findAll() {
        return SyncUtils.url2RouteList(findAllUrl());
    }

    /**
     * 获取  原子自增到路由URL的映射集合
     *
     * @return
     */
    private Map<Long, URL> findAllUrl() {
        Map<String, String> filter = new HashMap<>(16);
        filter.put(Constants.CATEGORY_KEY, Constants.ROUTERS_CATEGORY);

        return SyncUtils.filterFromCategory(getRegistryCache(), filter);
    }

    /**
     * 查询指定路由
     *
     * @param id
     * @return
     */
    @Override
    public Route findRoute(Long id) {
        return SyncUtils.url2Route(findRouteUrlPair(id));
    }

    /**
     * 获取指定路由的原子自增到路由URL的映射集合
     *
     * @param id
     * @return
     */
    public Pair<Long, URL> findRouteUrlPair(Long id) {
        return SyncUtils.filterFromCategory(getRegistryCache(), Constants.ROUTERS_CATEGORY, id);
    }

    /**
     * 获取指定路由的URL
     *
     * @param id
     * @return
     */
    private URL findRouteUrl(Long id) {
        return findRoute(id).toUrl();
    }


    /**
     * 多条件过滤路由
     *
     * @param service 服务名
     * @param address 地址
     * @param force   force
     * @return
     */
    private Map<Long, URL> findRouteUrl(String service, String address, boolean force) {
        Map<String, String> filter = new HashMap<String, String>();
        filter.put(Constants.CATEGORY_KEY, Constants.ROUTERS_CATEGORY);
        if (service != null && service.length() > 0) {
            filter.put(SyncUtils.SERVICE_FILTER_KEY, service);
        }
        if (address != null && address.length() > 0) {
            filter.put(SyncUtils.ADDRESS_FILTER_KEY, address);
        }
        if (force) {
            filter.put("force", "true");
        }
        return SyncUtils.filterFromCategory(getRegistryCache(), filter);
    }

    /**
     * 获取指定服务对应的路由
     *
     * @param serviceName
     * @return
     */
    @Override
    public List<Route> findByService(String serviceName) {
        return SyncUtils.url2RouteList(findRouteUrl(serviceName, null, false));
    }

    /**
     * 获取指定地址的路由
     *
     * @param address
     * @return
     */
    @Override
    public List<Route> findByAddress(String address) {
        return SyncUtils.url2RouteList(findRouteUrl(null, address, false));
    }

    /**
     * 获取服务和地址对应的路由
     *
     * @param service
     * @param address
     * @return
     */
    @Override
    public List<Route> findByServiceAndAddress(String service, String address) {
        return SyncUtils.url2RouteList(findRouteUrl(service, address, false));
    }

    /**
     * 根据服务过滤路由
     *
     * @param service
     * @return
     */
    @Override
    public List<Route> findForceRouteByService(String service) {
        return SyncUtils.url2RouteList(findRouteUrl(service, null, true));
    }

    /**
     * 获取指定地址对应的强制路由（当路由结果为空时，强制执行）
     *
     * @param address
     * @return
     */
    @Override
    public List<Route> findForceRouteByAddress(String address) {
        return SyncUtils.url2RouteList(findRouteUrl(null, address, true));
    }

    /**
     * 获取服务和地址对应的强制路由
     *
     * @param service
     * @param address
     * @return
     */
    @Override
    public List<Route> findForceRouteByServiceAndAddress(String service, String address) {
        return SyncUtils.url2RouteList(findRouteUrl(service, address, true));
    }

    /**
     * 获取所有的强制路由
     *
     * @return
     */
    @Override
    public List<Route> findAllForceRoute() {
        return SyncUtils.url2RouteList(findRouteUrl(null, null, true));
    }

}
