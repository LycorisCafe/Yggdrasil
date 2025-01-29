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
import io.github.lycoriscafe.nexus.http.core.requestMethods.annotations.DELETE;
import io.github.lycoriscafe.nexus.http.core.requestMethods.annotations.PATCH;
import io.github.lycoriscafe.nexus.http.core.requestMethods.annotations.POST;
import io.github.lycoriscafe.nexus.http.core.requestMethods.annotations.PUT;
import io.github.lycoriscafe.nexus.http.engine.reqResManager.httpReq.HttpDeleteRequest;
import io.github.lycoriscafe.nexus.http.engine.reqResManager.httpReq.HttpPatchRequest;
import io.github.lycoriscafe.nexus.http.engine.reqResManager.httpReq.HttpPostRequest;
import io.github.lycoriscafe.nexus.http.engine.reqResManager.httpReq.HttpPutRequest;
import io.github.lycoriscafe.nexus.http.engine.reqResManager.httpRes.HttpResponse;
import io.github.lycoriscafe.yggdrasil.authentication.Authentication;
import io.github.lycoriscafe.yggdrasil.authentication.AuthenticationService;
import io.github.lycoriscafe.yggdrasil.authentication.DeviceService;
import io.github.lycoriscafe.yggdrasil.authentication.Role;
import io.github.lycoriscafe.yggdrasil.commons.CommonService;
import io.github.lycoriscafe.yggdrasil.commons.ResponseModel;
import io.github.lycoriscafe.yggdrasil.commons.SearchModel;
import io.github.lycoriscafe.yggdrasil.configuration.Utils;
import io.github.lycoriscafe.yggdrasil.rest.admin.AccessLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.Set;

@HttpEndpoint("/teacher")
@Authenticated
public class TeacherEndpoint {
    private static final Logger logger = LoggerFactory.getLogger(TeacherEndpoint.class);

    @POST("/read")
    @ExpectContent("application/json")
    public static HttpResponse read(HttpPostRequest req,
                                    HttpResponse res) {
        var auth = AuthenticationService.authenticate(req, Set.of(Role.ADMIN, Role.TEACHER, Role.STUDENT), null);
        if (auth != null) return auth;

        try {
            SearchModel searchModel = SearchModel.fromJson(new String((byte[]) req.getContent().getData()));
            return res.setContent(CommonService.read(Teacher.class, TeacherService.class, searchModel).parse());
        } catch (Exception e) {
            logger.atError().log(e.getMessage());
            return res.setContent(new ResponseModel<Teacher>().setError(e.getMessage()).parse());
        }
    }

    @POST("/create")
    @ExpectContent("application/json")
    public static HttpResponse create(HttpPostRequest req,
                                      HttpResponse res) {
        var auth = AuthenticationService.authenticate(req, Set.of(Role.ADMIN), Set.of(AccessLevel.SUPERUSER, AccessLevel.TEACHER));
        if (auth != null) return auth;

        try {
            Teacher instance = Utils.getGson().fromJson(new String((byte[]) req.getContent().getData()), Teacher.class);
            ResponseModel<Teacher> response = CommonService.create(Teacher.class, TeacherService.class, instance);
            if (response.isSuccess()) {
                AuthenticationService.addAuthentication(
                        new Authentication(Role.TEACHER, response.getData().getFirst().getId(), "T" + response.getData().getFirst().getId()));
            }
            return res.setContent(response.parse());
        } catch (Exception e) {
            logger.atError().log(e.getMessage());
            return res.setContent(new ResponseModel<Teacher>().setError(e.getMessage()).parse());
        }
    }

    @PUT("/update")
    @ExpectContent("application/json")
    public static HttpResponse update(HttpPutRequest req,
                                      HttpResponse res) {
        var auth = AuthenticationService.authenticate(req, Set.of(Role.ADMIN), Set.of(AccessLevel.SUPERUSER, AccessLevel.TEACHER));
        if (auth != null) return auth;

        try {
            Teacher instance = Utils.getGson().fromJson(new String((byte[]) req.getContent().getData()), Teacher.class);
            return res.setContent(CommonService.update(Teacher.class, TeacherService.class, instance).parse());
        } catch (Exception e) {
            logger.atError().log(e.getMessage());
            return res.setContent(new ResponseModel<Teacher>().setError(e.getMessage()).parse());
        }
    }

    @DELETE("/delete")
    public static HttpResponse delete(HttpDeleteRequest req,
                                      HttpResponse res) {
        var auth = AuthenticationService.authenticate(req, Set.of(Role.ADMIN), Set.of(AccessLevel.SUPERUSER, AccessLevel.TEACHER));
        if (auth != null) return auth;

        if (req.getParameters() == null || !req.getParameters().containsKey("id")) {
            return res.setContent(new ResponseModel<Teacher>().setError("Required parameter 'id' is missing").parse());
        }
        try {
            BigInteger id = new BigInteger(req.getParameters().get("id"));
            ResponseModel<Teacher> response = CommonService.delete(Teacher.class, id);
            if (response.isSuccess()) {
                AuthenticationService.deleteAuthentication(Role.TEACHER, id);
            }
            return res.setContent(response.parse());
        } catch (Exception e) {
            logger.atError().log(e.getMessage());
            return res.setContent(new ResponseModel<Teacher>().setError(e.getMessage()).parse());
        }
    }

    @PATCH("/resetPassword")
    @ExpectContent("application/x-www-form-urlencoded")
    public static HttpResponse resetPassword(HttpPatchRequest req,
                                             HttpResponse res) {
        var auth = AuthenticationService.authenticate(req, Set.of(Role.ADMIN, Role.TEACHER),
                Set.of(AccessLevel.SUPERUSER, AccessLevel.TEACHER));
        if (auth != null) return auth;
        return res.setContent(AuthenticationService.updateAuthentication(req).parse());
    }

    @PATCH("/logout")
    @ExpectContent("none")
    public static HttpResponse logout(HttpPatchRequest req,
                                      HttpResponse res) {
        var auth = AuthenticationService.authenticate(req, Set.of(Role.ADMIN, Role.TEACHER), Set.of(AccessLevel.SUPERUSER, AccessLevel.TEACHER));
        if (auth != null) return auth;
        return res.setContent(DeviceService.removeDevice(req).parse());
    }
}
