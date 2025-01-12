/* Copyright 2023 predic8 GmbH, www.predic8.com

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License. */

package com.predic8.membrane.core.exceptions;

import com.predic8.membrane.core.interceptor.ratelimit.*;
import com.predic8.membrane.core.transport.*;
import com.predic8.membrane.core.util.*;
import org.apache.commons.lang3.exception.*;
import org.slf4j.*;
import org.springframework.beans.*;

import java.beans.*;

import static com.predic8.membrane.core.util.OSUtil.getOS;
import static java.util.Objects.requireNonNull;

public class SpringConfigurationErrorHandler {
    public static void handleRootCause(Exception e, Logger log) {
        Throwable root = ExceptionUtils.getRootCause(e);
        if (root instanceof PropertyBatchUpdateException pbue) {
            handlePropertyBatchUpdateException(log, pbue);
        } else if (root instanceof ConfigurationException ee) {
            handleConfigurationException(ee);
        } else if (root instanceof PortOccupiedException poe) {
            handlePortOccupiedException(poe);
        } else {
            e.printStackTrace();
        }
    }

    private static void handlePortOccupiedException(PortOccupiedException poe) {
        printStars();
        System.err.println();
        System.err.printf("Membrane is configured to open port %d. But this port is alreay in\n", poe.getPort());
        System.err.println("""
                use by a different program. To start Membrane do one of the following:
                                
                1. Find and stop the program that is occupying the port. Then restart Membrane.""");
        System.err.println();
        switch (getOS()) {
            case WINDOWS -> printHowToFindPortWindows();
            case LINUX, MAC -> printHowToFindPortLinux();
        }
        System.err.println("""       
                2. Configure Membrane to use a different port. Propably in the conf/proxies.xml
                file. Then restart Membrane.
                """);
    }

    private static void printHowToFindPortLinux() {
        System.err.println("""
                e.g.:
                > lsof -i :2000
                COMMAND    PID    USER  TYPE
                java     80910 predic8  IPv6  TCP  (LISTEN)
                > kill -9 80910
                """);
    }

    private static void printHowToFindPortWindows() {
        System.err.println("""
                netstat -aon | find /i "listening"
                """);
    }

    private static void handleConfigurationException(ConfigurationException ce) {
        printStars();
        System.err.println();
        System.err.println(ce.getMessage());
        System.err.println();
        System.err.println("giving up.");
    }

    private static void printStars() {
        System.err.println("**********************************************************************************");
    }

    private static void handlePropertyBatchUpdateException(Logger log, PropertyBatchUpdateException pbue) {
        for(Exception ie : pbue.getPropertyAccessExceptions()) {
            if (ie instanceof MethodInvocationException mie) {
                PropertyChangeEvent pce = mie.getPropertyChangeEvent();

                //noinspection SwitchStatementWithTooFewBranches
                switch (requireNonNull(pce).getPropertyName()) {
                    case "requestLimitDuration" -> RateLimitErrorHandling.handleRequestLimitDurationConfigurationException(log, pce);
                    default -> log.error("""
                            Invalid value %s for property %s.""".formatted(pce.getNewValue(),pce.getPropertyName()));

                }
            }
        }
    }
}