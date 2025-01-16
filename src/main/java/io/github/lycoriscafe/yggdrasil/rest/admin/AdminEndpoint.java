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
    /**
     * Get admins.
     * <p>
     * AccessLevel - {@code SUPERUSER}
     * <ul>
     *     <li>{@code GET [no content] @ /admin?id=#} - Get admin by ID</li>
     *     <li>{@code GET [no content] @ /admin[?resultsFrom=#,resultsOffset=#]} - Get all admins</li>
     * </ul>
     *
     * @param request  Nexus-HTTP HTTP Request
     * @param response Nexus-HTTP HTTP Response
     * @return {@code Response<Admin>}
     * @see AdminEndpoint
     * @since v1.0
     */
    @GET("/")
    public static HttpResponse getAdmins(HttpGetRequest request,
                                         HttpResponse response) {
        var auth = AuthenticationService.authenticate(request, Role.ADMIN, AccessLevel.SUPERUSER);
        if (auth != null) return auth;

        try {
            var id = request.getParameters().get("id");
            if (id == null) {
                var resultsFrom = request.getParameters().get("resultsFrom");
                var resultsOffset = request.getParameters().get("resultsOffset");
                return response.setContent(AdminService.getAllAdmins(resultsFrom == null ? null : Long.parseLong(resultsFrom),
                        resultsOffset == null ? null : Long.parseLong(resultsOffset)).parse());
            }
            return response.setContent(AdminService.getAdminById(Long.parseLong(id)).parse());
        } catch (NumberFormatException e) {
            return response.setContent(new Response<Admin>().setError("Invalid parameters").parse());
        }
    }

    /**
     * Insert/update admin.
     * <p>
     * AccessLevel - {@code SUPERUSER}
     * <ul>
     *     <li>{@code POST [multipart/form-data] @ /admin[?update=false]} - Insert admin</li>
     *     Required content parameters - {@code name}, {@code accessLevel}
     *     <li>{@code POST [multipart/form-data] @ /admin?update=true} - Update admin</li>
     *     Required content parameters - {@code id}
     * </ul>
     * Required content parameters -
     * <b>When inset admin, the ID (A#) will be the password.</b>
     *
     * @param request  Nexus-HTTP HTTP Request
     * @param response Nexus-HTTP HTTP Response
     * @return {@code Response<Admin>}
     * @see AdminEndpoint
     * @since v1.0
     */
    @POST("/")
    @ExpectContent("multipart/form-data")
    @SuppressWarnings("unchecked")
    public static HttpResponse updateAdmin(HttpPostRequest request,
                                           HttpResponse response) {
        var auth = AuthenticationService.authenticate(request, Role.ADMIN, AccessLevel.SUPERUSER);
        if (auth != null) return auth;

        try {
            var admin = Admin.toAdmin((List<MultipartFormData>) request.getContent().getData());
            var update = request.getParameters().get("update");
            if (Boolean.parseBoolean(update)) {
                if (admin.getId() == null) return response.setContent(new Response<Admin>().setError("Invalid ID").parse());
                return response.setContent(AdminService.updateAdminById(admin).parse());
            }
            if (admin.getName() == null || admin.getAccessLevel() == null) {
                return response.setContent(new Response<Admin>().setError("Missing content parameters").parse());
            }
            return response.setContent(AdminService.createAdmin(admin).parse());
        } catch (Exception e) {
            return response.setContent(new Response<Admin>().setError("Invalid form-data").parse());
        }
    }

    /**
     * Delete admin.
     * <p>
     * AccessLevel - {@code SUPERUSER}
     * <ul>
     *     <li>{@code DELETE [no content] @ /admin?id=#} - Delete admin</li>
     * </ul>
     *
     * @param request  Nexus-HTTP HTTP Request
     * @param response Nexus-HTTP HTTP Response
     * @return {@code Response<Admin>}
     * @see AdminEndpoint
     * @since v1.0
     */
    @DELETE("/")
    public static HttpResponse deleteAdmin(HttpDeleteRequest request,
                                           HttpResponse response) {
        var auth = AuthenticationService.authenticate(request, Role.ADMIN, AccessLevel.SUPERUSER);
        if (auth != null) return auth;

        try {
            var id = request.getParameters().get("id");
            if (id == null) {
                return response.setContent(new Response<Admin>().setError("ID not found").parse());
            }
            return response.setContent(AdminService.deleteAdminById(Long.parseLong(id)).parse());
        } catch (Exception e) {
            return response.setContent(new Response<Admin>().setError("Invalid parameters").parse());
        }
    }

    /**
     * Reset admin password.
     * <p>
     * AccessLevel - {@code SUPERUSER}
     * <ul>
     *     <li>{@code PATCH [application/x-www-form-urlencoded] @ /admin} - Reset password</li>
     *     Required content parameters - {@code id}, {@code oldPassword}, {@code newPassword}
     * </ul>
     *
     * @param request  Nexus-HTTP HTTP Request
     * @param response Nexus-HTTP HTTP Response
     * @return {@code Response<Admin>}
     * @see AdminEndpoint
     * @since v1.0
     */
    @PATCH("/")
    @ExpectContent("application/x-www-form-urlencoded")
    public static HttpResponse resetPassword(HttpPatchRequest request,
                                             HttpResponse response) {
        var auth = AuthenticationService.authenticate(request, Role.ADMIN, AccessLevel.SUPERUSER);
        if (auth != null) return auth;

        try {
            var data = (UrlEncodedData) request.getContent().getData();
            return response.setContent(AdminService.resetPassword(Long.parseLong(data.get("id")), data.get("oldPassword"), data.get("newPassword"))
                    .parse());
        } catch (Exception e) {
            return response.setContent(new Response<Admin>().setError("Invalid/missing parameters").parse());
        }
    }
}
