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

package io.github.lycoriscafe.yggdrasil.rest.admin;

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

import java.util.List;

/**
 * <ul>
 *     <li>{@code #} means {@code Java unsigned long}/{@code MySQL SERIAL} > {@code 0}</li>
 *     <li>{@code [...]} means optional parameters</li>
 * </ul>
 *
 * @see Admin
 * @see Response
 * @see AccessLevel
 * @since v1.0
 */
@HttpEndpoint("/admin")
@Authenticated
public class AdminEndpoint {
    @GET("/")
    public static HttpResponse getAdmins(HttpGetRequest request,
                                         HttpResponse response) {
        var auth = AuthenticationService.authenticate(request, new Role[]{Role.ADMIN}, AccessLevel.SUPERUSER);
        if (auth != null) return auth;

        if (request.getParameters() == null) {
            return response.setContent(AdminService.getAllAdmins(null, null).parse());
        }
        try {
            return response.setContent(AdminService.getAdmins(null, null, null, null, null,
                            request.getParameters().get("resultsFrom") == null ? null : Long.parseLong(request.getParameters().get("resultsFrom")),
                            request.getParameters().get("resultsOffset") == null ? null : Long.parseLong(request.getParameters().get("resultsOffset")))
                    .parse());
        } catch (NumberFormatException e) {
            return response.setContent(new Response<Admin>().setError(e.getMessage()).parse());
        }
    }

    @POST("/")
    @ExpectContent("multipart/form-data")
    @SuppressWarnings("unchecked")
    public static HttpResponse updateAdmin(HttpPostRequest request,
                                           HttpResponse response) {
        var auth = AuthenticationService.authenticate(request, new Role[]{Role.ADMIN}, AccessLevel.SUPERUSER);
        if (auth != null) return auth;

        var update = request.getParameters() == null ? null : request.getParameters().get("update") == null ?
                null : Boolean.parseBoolean(request.getParameters().get("update"));
        try {
            if (update == null || !update) {
                return response.setContent(AdminService.createAdmin(Admin.toAdmin((List<MultipartFormData>) request.getContent().getData())).parse());
            }
            return response.setContent(AdminService.updateAdmin(Admin.toAdmin((List<MultipartFormData>) request.getContent().getData())).parse());
        } catch (Exception e) {
            return response.setContent(new Response<Admin>().setError(e.getMessage()).parse());
        }
    }

    @DELETE("/")
    public static HttpResponse deleteAdmin(HttpDeleteRequest request,
                                           HttpResponse response) {
        var auth = AuthenticationService.authenticate(request, new Role[]{Role.ADMIN}, AccessLevel.SUPERUSER);
        if (auth != null) return auth;

        if (request.getParameters() == null) {
            return response.setContent(new Response<Admin>().setError("id parameter not found").parse());
        }
        try {
            return response.setContent(AdminService.deleteAdminById(Long.parseLong(request.getParameters().get("id"))).parse());
        } catch (Exception e) {
            return response.setContent(new Response<Admin>().setError(e.getMessage()).parse());
        }
    }

    @PATCH("/")
    @ExpectContent("application/x-www-form-urlencoded")
    public static HttpResponse resetPassword(HttpPatchRequest request,
                                             HttpResponse response) {
        var auth = AuthenticationService.authenticate(request, new Role[]{Role.ADMIN}, AccessLevel.SUPERUSER);
        if (auth != null) return auth;

        try {
            var data = (UrlEncodedData) request.getContent().getData();
            return response.setContent(AdminService.resetPassword(Long.parseLong(data.get("id")), data.get("oldPassword"), data.get("newPassword"))
                    .parse());
        } catch (Exception e) {
            return response.setContent(new Response<Admin>().setError(e.getMessage()).parse());
        }
    }
}
