/*******************************************************************************
 * Copyright (c) 2019, 2023 Red Hat, Inc. and others
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution,
 * and is available at https://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 * Red Hat, Inc. - initial API and implementation
 * IBM Corporation
 ******************************************************************************/

package io.openliberty.tools.intellij.lsp4jakarta.lsp;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.vfs.VirtualFile;
import io.openliberty.tools.intellij.lsp4jakarta.lsp4ij.PropertiesManagerForJakarta;
import io.openliberty.tools.intellij.lsp4mp.MicroProfileProjectService;
import org.microshed.lsp4ij.client.CoalesceByKey;
import org.microshed.lsp4ij.client.IndexAwareLanguageClient;
import io.openliberty.tools.intellij.lsp4mp4ij.psi.core.utils.IPsiUtils;
import io.openliberty.tools.intellij.lsp4mp4ij.psi.internal.core.ls.PsiUtilsLSImpl;
import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.PublishDiagnosticsParams;
import org.eclipse.lsp4jakarta.api.JakartaLanguageClientAPI;
import org.eclipse.lsp4jakarta.commons.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Adapted from https://github.com/redhat-developer/intellij-quarkus/blob/2585eb422beeb69631076d2c39196d6eca2f5f2e/src/main/java/com/redhat/devtools/intellij/quarkus/lsp/QuarkusLanguageClient.java
 * to match LSP4MP, Language Server for MicroProfile
 */
public class JakartaLanguageClient extends IndexAwareLanguageClient implements JakartaLanguageClientAPI, MicroProfileProjectService.Listener {
  private static final Logger LOGGER = LoggerFactory.getLogger(JakartaLanguageClient.class);

  public JakartaLanguageClient(Project project) {
    super(project);
  }

  // Support the message "jakarta/java/classpath"
  @Override
  public CompletableFuture<List<String>> getContextBasedFilter(JakartaClasspathParams classpathParams) {
    String uri = classpathParams.getUri();
    List<String> snippetContexts = classpathParams.getSnippetCtx();
    Project project = getProject();
    var coalesceBy = new CoalesceByKey("jakarta/java/classpath", uri, snippetContexts);
    return runAsBackground("Computing Jakarta context",
            monitor -> PropertiesManagerForJakarta.getInstance().getExistingContextsFromClassPath(uri, snippetContexts, project), coalesceBy);
  }

  // Support the message "jakarta/java/cursorcontext"
  @Override
  public CompletableFuture<JavaCursorContextResult> getJavaCursorContext(JakartaJavaCompletionParams params) {
    var coalesceBy = new CoalesceByKey("jakarta/java/cursorcontext", params.getUri(), params.getPosition());
    return runAsBackground("Computing Java cursor context",
            monitor -> {
              IPsiUtils utils = PsiUtilsLSImpl.getInstance(getProject());
              return PropertiesManagerForJakarta.getInstance().javaCursorContext(params, utils);
            }, coalesceBy);
  }

  // Support the message "jakarta/java/diagnostics"
  @Override
  public CompletableFuture<List<PublishDiagnosticsParams>> getJavaDiagnostics(JakartaDiagnosticsParams jakartaParams) {
    var coalesceBy = new CoalesceByKey("jakarta/java/diagnostics", jakartaParams.getUris());
    IPsiUtils utils = PsiUtilsLSImpl.getInstance(getProject());
    return runAsBackground("Computing Jakarta Java diagnostics",
            monitor -> PropertiesManagerForJakarta.getInstance().diagnostics(jakartaParams, utils), coalesceBy);
  }

  // Support the message "jakarta/java/codeaction
  public CompletableFuture<List<CodeAction>> getCodeAction(JakartaJavaCodeActionParams params) {
    IPsiUtils utils = PsiUtilsLSImpl.getInstance(getProject());
    var coalesceBy = new CoalesceByKey("jakarta/java/codeAction", params.getUri());
    return runAsBackground("Computing Jakarta code actions",
            monitor -> PropertiesManagerForJakarta.getInstance().getCodeAction(params, utils), coalesceBy);
  }

  @Override
  public void libraryUpdated(Library library) {
    // not needed for Jakarta LS
  }

  @Override
  public void sourceUpdated(List<Pair<Module, VirtualFile>> sources) {
    int i = 0;
    // not needed for Jakarta LS
  }
}
