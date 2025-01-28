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

package io.github.lycoriscafe.yggdrasil.rest.student;

import io.github.lycoriscafe.nexus.http.core.HttpEndpoint;
import io.github.lycoriscafe.nexus.http.core.headers.auth.Authenticated;
import io.github.lycoriscafe.nexus.http.core.headers.content.ExpectContent;
import io.github.lycoriscafe.nexus.http.core.headers.content.MultipartFormData;
import io.github.lycoriscafe.nexus.http.core.headers.content.UrlEncodedData;
import io.github.lycoriscafe.nexus.http.core.requestMethods.annotations.DELETE;
import io.github.lycoriscafe.nexus.http.core.requestMethods.annotations.GET;
import io.github.lycoriscafe.nexus.http.core.requestMethods.annotations.PATCH;
import io.github.lycoriscafe.nexus.http.core.requestMethods.annotations.POST;
import io.github.lycoriscafe.nexus.http.engine.reqResManager.httpReq.HttpDeleteRequest;
import io.github.lycoriscafe.nexus.http.engine.reqResManager.httpReq.HttpGetRequest;
import io.github.lycoriscafe.nexus.http.engine.reqResManager.httpReq.HttpPatchRequest;
import io.github.lycoriscafe.nexus.http.engine.reqResManager.httpReq.HttpPostRequest;
import io.github.lycoriscafe.nexus.http.engine.reqResManager.httpRes.HttpResponse;
import io.github.lycoriscafe.yggdrasil.authentication.AuthenticationService;
import io.github.lycoriscafe.yggdrasil.authentication.Role;
import io.github.lycoriscafe.yggdrasil.commons.Response;
import io.github.lycoriscafe.yggdrasil.commons.SearchQueryBuilder;
import io.github.lycoriscafe.yggdrasil.commons.UpdateQueryBuilder;
import io.github.lycoriscafe.yggdrasil.rest.admin.AccessLevel;

import java.math.BigInteger;
import java.util.List;

@HttpEndpoint("/student")
@Authenticated
public class StudentEndpoint {
    @GET("/read")
    public static HttpResponse read(HttpGetRequest req,
                                    HttpResponse res) {
        var auth = AuthenticationService.authenticate(req, new Role[]{Role.ADMIN, Role.TEACHER, Role.STUDENT},
                AccessLevel.SUPERUSER, AccessLevel.STUDENT);
        if (auth != null) return auth;

        return res.setContent(StudentService.select(SearchQueryBuilder.build(
                Student.class, StudentService.Columns.class, StudentService.class,
                req.getParameters())).parse());
    }

    @POST("/create")
    @ExpectContent("multipart/form-data")
    @SuppressWarnings("unchecked")
    public static HttpResponse create(HttpPostRequest req,
                                      HttpResponse res) {
        var auth = AuthenticationService.authenticate(req, new Role[]{Role.ADMIN},
                AccessLevel.SUPERUSER, AccessLevel.STUDENT);
        if (auth != null) return auth;

        return res.setContent(StudentService.insert(UpdateQueryBuilder.build(
                Student.class, StudentService.Columns.class, StudentService.class,
                req.getParameters(), (List<MultipartFormData>) req.getContent().getData())).parse());
    }

    @POST("/update")
    @ExpectContent("multipart/form-data")
    @SuppressWarnings("unchecked")
    public static HttpResponse update(HttpPostRequest req,
                                      HttpResponse res) {
        var auth = AuthenticationService.authenticate(req, new Role[]{Role.ADMIN},
                AccessLevel.SUPERUSER, AccessLevel.STUDENT);
        if (auth != null) return auth;

        return res.setContent(StudentService.update(UpdateQueryBuilder.build(
                Student.class, StudentService.Columns.class, StudentService.class,
                req.getParameters(), (List<MultipartFormData>) req.getContent().getData())).parse());
    }

    @DELETE("/delete")
    public static HttpResponse delete(HttpDeleteRequest req,
                                      HttpResponse res) {
        var auth = AuthenticationService.authenticate(req, new Role[]{Role.ADMIN},
                AccessLevel.SUPERUSER, AccessLevel.STUDENT);
        if (auth != null) return auth;

        return res.setContent(StudentService.delete(SearchQueryBuilder.build(
                Student.class, StudentService.Columns.class, StudentService.class,
                req.getParameters())).parse());
    }

    @PATCH("/resetPassword")
    @ExpectContent("application/x-www-form-urlencoded")
    public static HttpResponse resetPassword(HttpPatchRequest req,
                                             HttpResponse res) {
        var auth = AuthenticationService.authenticate(req, new Role[]{Role.ADMIN, Role.STUDENT},
                AccessLevel.SUPERUSER, AccessLevel.STUDENT);
        if (auth != null) return auth;

        return res.setContent(StudentService.resetPassword((UrlEncodedData) req.getContent().getData()).parse());
    }

    @PATCH("/logout")
    public static HttpResponse logout(HttpPatchRequest req,
                                      HttpResponse res) {
        var auth = AuthenticationService.authenticate(req, new Role[]{Role.ADMIN, Role.STUDENT},
                AccessLevel.SUPERUSER, AccessLevel.STUDENT);
        if (auth != null) return auth;

        try {
            return res.setContent(AuthenticationService.logout(Role.STUDENT, new BigInteger(req.getParameters().get("id"))).parse());
        } catch (Exception e) {
            return res.setContent(new Response<>().setError("Unparseable id").parse());
        }
    }
}
