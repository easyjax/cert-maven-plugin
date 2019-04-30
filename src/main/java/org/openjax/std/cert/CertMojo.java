/* Copyright (c) 2006 OpenJAX
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * You should have received a copy of The MIT License (MIT) along with this
 * program. If not, see <http://opensource.org/licenses/MIT/>.
 */

package org.openjax.std.cert;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.maven.model.Repository;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Execute;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.openjax.ext.maven.mojo.BaseMojo;

@Mojo(name="import", defaultPhase=LifecyclePhase.INITIALIZE)
@Execute(goal="import")
public class CertMojo extends BaseMojo {
  private final Set<String> checkedURLs = new HashSet<>();

  @Parameter(property = "password")
  private String password;

  @Parameter(property="project.repositories", readonly=true, required=true)
  private List<Repository> repositories;

  public List<Repository> getRepositories() {
    return repositories;
  }

  @Override
  public void execute(final boolean failOnNoOp) throws MojoExecutionException, MojoFailureException {
    boolean modified = false;
    for (final Repository repository : getRepositories()) {
      if (!repository.getUrl().startsWith("https") || checkedURLs.contains(repository.getUrl()))
        continue;

      try {
        final URL url = new URL(repository.getUrl());
        getLog().info(url.getHost() + ":" + url.getPort());
        modified |= InstallCert.install(url.getHost(), url.getPort() != -1 ? url.getPort() : 443, password == null ? null : password.toCharArray());
        checkedURLs.add(repository.getUrl());
      }
      catch (final FileNotFoundException e) {
        if (!e.getMessage().contains("(Permission denied)"))
          throw new MojoFailureException("Failure due to " + InstallCert.class.getSimpleName(), e);

        getLog().error("Attempting to modify JDK CA certificates file " + e.getMessage());
        getLog().error("Please run the same command as root, via \"sudo\".");
        return;
      }
      catch (final GeneralSecurityException | IOException e) {
        throw new MojoExecutionException("Failure due to " + InstallCert.class.getSimpleName(), e);
      }
    }

    if (!modified && failOnNoOp)
      throw new MojoExecutionException("Certificate not installed (failOnNoOp=true)");
  }
}