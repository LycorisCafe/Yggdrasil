/*
 * Copyright 2025 Lycoris Café
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.lycoriscafe.yggdrasil.rest.notification;

import io.github.lycoriscafe.nexus.http.core.HttpEndpoint;
import io.github.lycoriscafe.nexus.http.core.headers.auth.Authenticated;
import io.github.lycoriscafe.nexus.http.core.headers.content.ExpectContent;
import io.github.lycoriscafe.nexus.http.core.headers.content.MultipartFormData;
import io.github.lycoriscafe.nexus.http.core.requestMethods.annotations.DELETE;
import io.github.lycoriscafe.nexus.http.core.requestMethods.annotations.GET;
import io.github.lycoriscafe.nexus.http.core.requestMethods.annotations.POST;
import io.github.lycoriscafe.nexus.http.engine.reqResManager.httpReq.HttpDeleteRequest;
import io.github.lycoriscafe.nexus.http.engine.reqResManager.httpReq.HttpGetRequest;
import io.github.lycoriscafe.nexus.http.engine.reqResManager.httpReq.HttpPostRequest;
import io.github.lycoriscafe.nexus.http.engine.reqResManager.httpRes.HttpResponse;
import io.github.lycoriscafe.yggdrasil.authentication.AuthenticationService;
import io.github.lycoriscafe.yggdrasil.authentication.Role;
import io.github.lycoriscafe.yggdrasil.commons.SearchQueryBuilder;
import io.github.lycoriscafe.yggdrasil.commons.UpdateQueryBuilder;
import io.github.lycoriscafe.yggdrasil.rest.admin.AccessLevel;

import java.util.List;

@HttpEndpoint("/notification")
@Authenticated
public class NotificationEndpoint {
    @GET("/read")
    public static HttpResponse read(HttpGetRequest req,
                                    HttpResponse res) {
        var auth = AuthenticationService.authenticate(req, new Role[]{Role.ADMIN, Role.TEACHER, Role.STUDENT},
                AccessLevel.SUPERUSER, AccessLevel.NOTIFICATION);
        if (auth != null) return auth;

        return res.setContent(NotificationService.select(SearchQueryBuilder.build(
                Notification.class, NotificationService.Columns.class, NotificationService.class,
                req.getParameters())).parse());
    }

    @POST("/create")
    @ExpectContent("multipart/form-data")
    @SuppressWarnings("unchecked")
    public static HttpResponse create(HttpPostRequest req,
                                      HttpResponse res) {
        var auth = AuthenticationService.authenticate(req, new Role[]{Role.ADMIN},
                AccessLevel.SUPERUSER, AccessLevel.NOTIFICATION);
        if (auth != null) return auth;

        return res.setContent(NotificationService.insert(UpdateQueryBuilder.build(
                Notification.class, NotificationService.Columns.class, NotificationService.class,
                req.getParameters(), (List<MultipartFormData>) req.getContent().getData())).parse());
    }

    @POST("/update")
    @ExpectContent("multipart/form-data")
    @SuppressWarnings("unchecked")
    public static HttpResponse update(HttpPostRequest req,
                                      HttpResponse res) {
        var auth = AuthenticationService.authenticate(req, new Role[]{Role.ADMIN},
                AccessLevel.SUPERUSER, AccessLevel.NOTIFICATION);
        if (auth != null) return auth;

        return res.setContent(NotificationService.update(UpdateQueryBuilder.build(
                Notification.class, NotificationService.Columns.class, NotificationService.class,
                req.getParameters(), (List<MultipartFormData>) req.getContent().getData())).parse());
    }

    @DELETE("/delete")
    public static HttpResponse delete(HttpDeleteRequest req,
                                      HttpResponse res) {
        var auth = AuthenticationService.authenticate(req, new Role[]{Role.ADMIN},
                AccessLevel.SUPERUSER, AccessLevel.NOTIFICATION);
        if (auth != null) return auth;

        return res.setContent(NotificationService.delete(SearchQueryBuilder.build(
                Notification.class, NotificationService.Columns.class, NotificationService.class,
                req.getParameters())).parse());
    }
}
