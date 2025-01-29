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
import io.github.lycoriscafe.nexus.http.core.requestMethods.annotations.POST;
import io.github.lycoriscafe.nexus.http.engine.reqResManager.httpReq.HttpPostRequest;
import io.github.lycoriscafe.nexus.http.engine.reqResManager.httpRes.HttpResponse;
import io.github.lycoriscafe.yggdrasil.authentication.AuthenticationService;
import io.github.lycoriscafe.yggdrasil.authentication.DeviceService;
import io.github.lycoriscafe.yggdrasil.authentication.Role;
import io.github.lycoriscafe.yggdrasil.commons.CommonService;
import io.github.lycoriscafe.yggdrasil.commons.RequestModel;
import io.github.lycoriscafe.yggdrasil.commons.Response;
import io.github.lycoriscafe.yggdrasil.rest.admin.AccessLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

@HttpEndpoint("/student")
@Authenticated
public class StudentEndpoint {
    private static final Logger logger = LoggerFactory.getLogger(StudentEndpoint.class);

    @POST("/read")
    @ExpectContent("application/json")
    public static HttpResponse read(HttpPostRequest req,
                                    HttpResponse res) {
        var auth = AuthenticationService.authenticate(req, Set.of(Role.ADMIN, Role.TEACHER, Role.STUDENT), null);
        if (auth != null) return auth;

        try {
            RequestModel<Student> requestModel = RequestModel.fromJson(Student.class, new String((byte[]) req.getContent().getData()));
            return res.setContent(CommonService.read(Student.class, requestModel).parse());
        } catch (Exception e) {
            logger.atError().log(e.getMessage());
            return res.setContent(new Response<Student>().setError(e.getMessage()).parse());
        }
    }

    @POST("/create")
    @ExpectContent("application/json")
    public static HttpResponse create(HttpPostRequest req,
                                      HttpResponse res) {
        var auth = AuthenticationService.authenticate(req, Set.of(Role.ADMIN), Set.of(AccessLevel.SUPERUSER, AccessLevel.STUDENT));
        if (auth != null) return auth;

        try {
            RequestModel<Student> requestModel = RequestModel.fromJson(Student.class, new String((byte[]) req.getContent().getData()));
            return res.setContent(CommonService.create(Student.class, requestModel).parse());
        } catch (Exception e) {
            logger.atError().log(e.getMessage());
            return res.setContent(new Response<Student>().setError(e.getMessage()).parse());
        }
    }

    @POST("/update")
    @ExpectContent("application/json")
    public static HttpResponse update(HttpPostRequest req,
                                      HttpResponse res) {
        var auth = AuthenticationService.authenticate(req, Set.of(Role.ADMIN), Set.of(AccessLevel.SUPERUSER, AccessLevel.STUDENT));
        if (auth != null) return auth;

        try {
            RequestModel<Student> requestModel = RequestModel.fromJson(Student.class, new String((byte[]) req.getContent().getData()));
            return res.setContent(CommonService.update(Student.class, requestModel).parse());
        } catch (Exception e) {
            logger.atError().log(e.getMessage());
            return res.setContent(new Response<Student>().setError(e.getMessage()).parse());
        }
    }

    @POST("/delete")
    @ExpectContent("application/json")
    public static HttpResponse delete(HttpPostRequest req,
                                      HttpResponse res) {
        var auth = AuthenticationService.authenticate(req, Set.of(Role.ADMIN), Set.of(AccessLevel.SUPERUSER, AccessLevel.STUDENT));
        if (auth != null) return auth;

        try {
            RequestModel<Student> requestModel = RequestModel.fromJson(Student.class, new String((byte[]) req.getContent().getData()));
            return res.setContent(CommonService.delete(Student.class, requestModel).parse());
        } catch (Exception e) {
            logger.atError().log(e.getMessage());
            return res.setContent(new Response<Student>().setError(e.getMessage()).parse());
        }
    }

    @POST("/resetPassword")
    @ExpectContent("application/x-www-form-urlencoded")
    public static HttpResponse resetPassword(HttpPostRequest req,
                                             HttpResponse res) {
        var auth = AuthenticationService.authenticate(req, Set.of(Role.ADMIN, Role.STUDENT),
                Set.of(AccessLevel.SUPERUSER, AccessLevel.STUDENT));
        if (auth != null) return auth;
        return res.setContent(AuthenticationService.updateAuthentication(req).parse());
    }

    @POST("/logout")
    @ExpectContent("none")
    public static HttpResponse logout(HttpPostRequest req,
                                      HttpResponse res) {
        var auth = AuthenticationService.authenticate(req, Set.of(Role.ADMIN, Role.STUDENT),
                Set.of(AccessLevel.SUPERUSER, AccessLevel.STUDENT));
        if (auth != null) return auth;
        return res.setContent(DeviceService.removeDevice(req).parse());
    }
}
