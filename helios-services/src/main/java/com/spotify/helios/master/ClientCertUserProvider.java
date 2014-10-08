/*
 * Copyright (c) 2014 Spotify AB.
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.spotify.helios.master;

import com.google.common.base.Joiner;

import com.spotify.helios.master.resources.RequestUser;
import com.sun.jersey.api.core.HttpContext;
import com.sun.jersey.core.spi.component.ComponentContext;
import com.sun.jersey.core.spi.component.ComponentScope;
import com.sun.jersey.server.impl.inject.AbstractHttpContextInjectable;
import com.sun.jersey.spi.inject.Injectable;
import com.sun.jersey.spi.inject.InjectableProvider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Type;

import javax.ws.rs.ext.Provider;

@Provider
public class ClientCertUserProvider extends AbstractHttpContextInjectable<String>
    implements InjectableProvider<RequestUser, Type> {
  private static final Logger log = LoggerFactory.getLogger(ClientCertUserProvider.class);

  @Override
  public String getValue(HttpContext context) {
    log.warn("CERT {}", context.getClass());
    return "FRED";
//
//    X509Certificate[] certs =
//         // Comes from org.mortbay.http.JsseListener
//            (X509Certificate[]) request.getAttribute("javax.servlet.request.X509Certificate");
//
//        if (certs != null) {
//          for (int n = 0; n < certs.length; n++) {
//            final X509Certificate x509Certificate = certs[n];
//            log.warn("SSL Client cert subject: " + x509Certificate.getSubjectDN().toString());
//            log.warn("kind of thing {}", x509Certificate.getSubjectDN().getClass());
//            log.warn("Issuer {}", x509Certificate.getIssuerX500Principal());
//          }
//        }
//


  }

  @Override
  public Injectable<?> getInjectable(ComponentContext ctx, RequestUser ruAnnotation, Type type) {
    if (type.equals(String.class)) {
      return this;
    }
    return null;
  }

  @Override
  public ComponentScope getScope() {
    return ComponentScope.PerRequest;
  }
}
