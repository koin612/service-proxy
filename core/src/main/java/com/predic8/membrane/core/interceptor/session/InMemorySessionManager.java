/* Copyright 2021 predic8 GmbH, www.predic8.com

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License. */

package com.predic8.membrane.core.interceptor.session;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.predic8.membrane.annot.MCAttribute;
import com.predic8.membrane.annot.MCElement;
import com.predic8.membrane.core.Router;
import com.predic8.membrane.core.exchange.Exchange;
import com.predic8.membrane.core.http.Header;
import com.predic8.membrane.core.http.HeaderField;
import com.predic8.membrane.core.http.HeaderName;
import com.predic8.membrane.core.interceptor.session.Session;
import com.predic8.membrane.core.interceptor.session.SessionManager;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@MCElement(name = "inMemorySessionManager2")
public class InMemorySessionManager extends SessionManager {

    final static String ID_NAME = "_in_memory_session_id";
    Cache<String, Session> sessions;

    protected String cookieNamePrefix = UUID.randomUUID().toString().substring(0,8);

    @Override
    public void init(Router router) throws Exception {
        sessions = CacheBuilder.newBuilder()
                .expireAfterAccess(Duration.ofSeconds(getExpiresAfterSeconds()))
                .build();
    }

    @Override
    protected Map<String, Object> cookieValueToAttributes(String cookie) {
        try {
            synchronized (sessions) {
                return sessions.get(cookie.split("=true")[0], () -> new Session(usernameKeyName, new HashMap<>())).get();
            }
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected Map<Session, String> getCookieValues(Session... session) {
        createSessionIdsForNewSessions(session);
        fixMergedSessionId(session);

        synchronized (sessions) {
            addSessionToCache(session);
        }

        return mapSessionToName(session);
    }

    private Map<Session, String> mapSessionToName(Session[] session) {
        return Arrays.stream(session)
                .collect(Collectors.toMap(s -> s, s -> s.get(ID_NAME)));
    }

    private void addSessionToCache(Session[] session) {
        Arrays.stream(session).forEach(s -> sessions.put(s.get(ID_NAME), s));
    }

    private void createSessionIdsForNewSessions(Session[] session) {
        Arrays.stream(session).filter(s -> s.get(ID_NAME) == null).forEach(s -> s.put(ID_NAME, cookieNamePrefix + "-" +UUID.randomUUID().toString()));
    }

    private void fixMergedSessionId(Session[] session) {
        Arrays.stream(session)
                .filter(s -> s.get(ID_NAME).toString().contains(","))
                .forEach(s -> s.put(ID_NAME, cookieNamePrefix + "-" +UUID.randomUUID().toString()));
    }

    @Override
    public List<String> getInvalidCookies(Exchange exc, String validCookie) {
        List<HeaderField> values = exc.getRequest().getHeader().getValues(new HeaderName(Header.COOKIE));
        return values.stream()
                .filter(hf -> hf.getValue().startsWith(cookieNamePrefix))
                .map(hf -> hf.getValue()).filter(value -> !value.contains(validCookie))
                .collect(Collectors.toList());
    }

    @Override
    protected boolean isValidCookieForThisSessionManager(String cookie) {
        synchronized (sessions) {
            return cookie.startsWith(cookieNamePrefix) && sessions.getIfPresent(cookie.split("=true")[0]) != null;
        }
    }

    @Override
    protected boolean cookieRenewalNeeded(String originalCookie) {
        synchronized (sessions) {
            return sessions.getIfPresent(originalCookie) != null;
        }
    }


}
