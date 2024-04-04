/*******************************************************************************
 * Copyright (c) 2024 IBM Corporation and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package io.openliberty.tools.intellij.lsp4mp.lsp;

import com.intellij.openapi.project.Project;
import org.eclipse.lsp4j.services.LanguageServer;
import org.eclipse.lsp4mp.ls.api.MicroProfileLanguageServerAPI;
import org.microshed.lsp4ij.LanguageServerFactory;
import org.microshed.lsp4ij.client.LanguageClientImpl;
import org.microshed.lsp4ij.server.StreamConnectionProvider;

public class MircoProfileLanguageServerFactory implements LanguageServerFactory {
    @Override
    public StreamConnectionProvider createConnectionProvider(Project project) {
        return new MicroProfileServer();
    }

    @Override
    public LanguageClientImpl createLanguageClient(Project project) {
        return new MicroProfileLanguageClient(project);
    }

    @Override
    public Class<? extends LanguageServer> getServerInterface() {
        return MicroProfileLanguageServerAPI.class;
    }
}
