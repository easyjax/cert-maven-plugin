/* Copyright 2006 Sun Microsystems, Inc.  All Rights Reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *   - Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *
 *   - Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *
 *   - Neither the name of Sun Microsystems nor the names of its
 *     contributors may be used to endorse or promote products derived
 *     from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, final STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.libx4j.maven.plugin.cert;

import java.io.FileNotFoundException;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.maven.model.Repository;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Execute;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

@Mojo(name = "import", defaultPhase = LifecyclePhase.INITIALIZE)
@Execute(goal = "import")
public final class CertMojo extends AbstractMojo {
  public static final Set<String> checkedURLs = new HashSet<String>();

  @Parameter(property = "project.repositories", readonly = true, required = true)
  private boolean mavenTestSkip;
  private List<Repository> repositories;

  public List<Repository> getRepositories() {
    return repositories;
  }

  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {
    for (final Repository repository : getRepositories()) {
      if (repository.getUrl().startsWith("https") && !checkedURLs.contains(repository.getUrl())) {
        try {
          final URL url = new URL(repository.getUrl());
          getLog().info(url.getHost() + ":" + url.getPort());
          String arg = url.getHost();
          if (url.getPort() != -1)
            arg += ":" + url.getPort();

          InstallCert.main(new String[] {arg});
          checkedURLs.add(repository.getUrl());
        }
        catch (final FileNotFoundException e) {
          if (!e.getMessage().contains("(Permission denied)"))
            throw new MojoFailureException("Failure due to InstallCert", e);

          getLog().error("Attempting to modify JDK CA certificates file " + e.getMessage());
          getLog().error("Please run the same command as root, via \"sudo\".");
          System.exit(0);
        }
        catch (final Exception e) {
          throw new MojoExecutionException("Failure due to InstallCert", e);
        }
      }
    }
  }
}