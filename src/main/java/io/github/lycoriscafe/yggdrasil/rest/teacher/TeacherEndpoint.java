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

package io.github.lycoriscafe.yggdrasil.rest.teacher;

import io.github.lycoriscafe.nexus.http.core.HttpEndpoint;
import io.github.lycoriscafe.nexus.http.core.headers.auth.Authenticated;
import io.github.lycoriscafe.nexus.http.core.headers.content.ExpectContent;
import io.github.lycoriscafe.nexus.http.core.headers.content.MultipartFormData;
import io.github.lycoriscafe.nexus.http.core.headers.content.UrlEncodedData;
import io.github.lycoriscafe.nexus.http.core.requestMethods.annotations.DELETE;
import io.github.lycoriscafe.nexus.http.core.requestMethods.annotations.GET;
import io.github.lycoriscafe.nexus.http.core.requestMethods.annotations.PATCH;
import io.github.lycoriscafe.nexus.http.core.requestMethods.annotations.POST;
import io.github.lycoriscafe.nexus.http.engine.ReqResManager.httpReq.HttpDeleteRequest;
import io.github.lycoriscafe.nexus.http.engine.ReqResManager.httpReq.HttpGetRequest;
import io.github.lycoriscafe.nexus.http.engine.ReqResManager.httpReq.HttpPatchRequest;
import io.github.lycoriscafe.nexus.http.engine.ReqResManager.httpReq.HttpPostRequest;
import io.github.lycoriscafe.nexus.http.engine.ReqResManager.httpRes.HttpResponse;
import io.github.lycoriscafe.yggdrasil.authentication.AuthenticationService;
import io.github.lycoriscafe.yggdrasil.authentication.Role;
import io.github.lycoriscafe.yggdrasil.configuration.Response;
import io.github.lycoriscafe.yggdrasil.configuration.commons.SearchQueryBuilder;
import io.github.lycoriscafe.yggdrasil.configuration.commons.UpdateQueryBuilder;
import io.github.lycoriscafe.yggdrasil.rest.admin.AccessLevel;

import java.math.BigDecimal;
import java.util.List;

@HttpEndpoint("/teacher")
@Authenticated
public class TeacherEndpoint {
    @GET("/read")
    public static HttpResponse read(HttpGetRequest req,
                                    HttpResponse res) {
        var auth = AuthenticationService.authenticate(req, new Role[]{Role.ADMIN, Role.TEACHER, Role.STUDENT},
                AccessLevel.SUPERUSER, AccessLevel.TEACHER_READ, AccessLevel.TEACHER_WRITE);
        if (auth != null) return auth;

        return res.setContent(TeacherService.select(SearchQueryBuilder.build(Teacher.class, TeacherService.Columns.class, TeacherService.class,
                req.getParameters())).parse());
    }

    @POST("/create")
    @ExpectContent("multipart/form-data")
    @SuppressWarnings("unchecked")
    public static HttpResponse create(HttpPostRequest req,
                                      HttpResponse res) {
        var auth = AuthenticationService.authenticate(req, new Role[]{Role.ADMIN},
                AccessLevel.SUPERUSER, AccessLevel.TEACHER_WRITE);
        if (auth != null) return auth;

        return res.setContent(TeacherService.insert(UpdateQueryBuilder.build(Teacher.class, TeacherService.Columns.class, TeacherService.class,
                req.getParameters(), (List<MultipartFormData>) req.getContent().getData())).parse());
    }

    @POST("/update")
    @ExpectContent("multipart/form-data")
    @SuppressWarnings("unchecked")
    public static HttpResponse update(HttpPostRequest req,
                                      HttpResponse res) {
        var auth = AuthenticationService.authenticate(req, new Role[]{Role.ADMIN},
                AccessLevel.SUPERUSER, AccessLevel.TEACHER_WRITE);
        if (auth != null) return auth;

        return res.setContent(TeacherService.update(UpdateQueryBuilder.build(Teacher.class, TeacherService.Columns.class, TeacherService.class,
                req.getParameters(), (List<MultipartFormData>) req.getContent().getData())).parse());
    }

    @DELETE("/delete")
    public static HttpResponse delete(HttpDeleteRequest req,
                                      HttpResponse res) {
        var auth = AuthenticationService.authenticate(req, new Role[]{Role.ADMIN},
                AccessLevel.SUPERUSER, AccessLevel.TEACHER_WRITE);
        if (auth != null) return auth;

        return res.setContent(TeacherService.delete(SearchQueryBuilder.build(Teacher.class, TeacherService.Columns.class, TeacherService.class,
                req.getParameters())).parse());
    }

    @PATCH("/resetPassword")
    @ExpectContent("application/x-www-form-urlencoded")
    public static HttpResponse resetPassword(HttpPatchRequest req,
                                             HttpResponse res) {
        var auth = AuthenticationService.authenticate(req, new Role[]{Role.ADMIN, Role.STUDENT},
                AccessLevel.SUPERUSER, AccessLevel.TEACHER_WRITE);
        if (auth != null) return auth;

        return res.setContent(TeacherService.resetPassword((UrlEncodedData) req.getContent().getData()).parse());
    }

    @PATCH("/logout")
    @ExpectContent("application/x-www-form-urlencoded")
    public static HttpResponse logout(HttpPatchRequest req,
                                      HttpResponse res) {
        var auth = AuthenticationService.authenticate(req, new Role[]{Role.ADMIN, Role.STUDENT},
                AccessLevel.SUPERUSER, AccessLevel.TEACHER_WRITE);
        if (auth != null) return auth;

        try {
            return res.setContent(AuthenticationService.logout(Role.TEACHER, new BigDecimal(req.getParameters().get("id"))).parse());
        } catch (Exception e) {
            return res.setContent(new Response<>().setError("Unparseable id").parse());
        }
    }
}
