package de.uni_mannheim.informatik.dws.melt.matching_maven_plugin;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.SourceVersion;
import org.apache.commons.codec.binary.Base64;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.settings.Server;
import org.apache.maven.settings.Settings;
import org.gitlab4j.api.Constants.Encoding;
import org.gitlab4j.api.GitLabApi;
import org.gitlab4j.api.GitLabApiException;
import org.gitlab4j.api.ProjectApi;
import org.gitlab4j.api.RepositoryFileApi;
import org.gitlab4j.api.models.Project;
import org.gitlab4j.api.models.RepositoryFile;
import org.gitlab4j.api.models.Visibility;


/**
 * This Mojo will:
 *   1. create a project (if not already existent) in gitlab with the name of the artifactId
 *   2. create or update a system.ttl file which describes the matching system so that version and implementing benchmarks are updated
 *   3. set the maven property ${hobbit.imageURL} to the correct value which is normally "git.project-hobbit.eu:4567/${username}/${artifactId}:${project.version}
 */
@Mojo( name = "prepareHobbitGitlab", defaultPhase = LifecyclePhase.VERIFY)
public class PrepareHobbitGitlab extends AbstractMojo{
    private static final String NEWLINE = System.getProperty("line.separator");
    
    @Parameter( property = "authConfig")
    private AuthConfig authConfig;
    
    @Parameter( property = "giturl", defaultValue = "https://git.project-hobbit.eu")
    private String giturl;
    
    @Parameter( property = "registryPort", defaultValue ="4567")
    private String registryPort;
    
    @Parameter( property = "defaultbranch", defaultValue ="master")
    private String defaultbranch;
    
    @Parameter( property = "benchmarks", defaultValue ="")
    private String[] benchmarks;
    
    
    //maven specific attributes
    @Parameter( defaultValue = "${project.artifactId}", readonly = true )
    private String projectArtifactId;//used as name for the matcher
    
    @Parameter( defaultValue = "${project.version}", readonly = true )
    private String projectVersion;
    
    @Parameter( defaultValue = "${project.description}", readonly = true )
    private String projectDescription;
    
    @Parameter( defaultValue = "${project}", readonly = true)
    private MavenProject mavenProject;
    
    @Parameter( defaultValue = "${settings}", readonly = true)
    private Settings settings;
   
    
    @Override
    public void execute() throws MojoExecutionException {
        String gitHost = getHost(this.giturl);
        GitLabApi gitLabApi = makeGitLabConnection(gitHost);
        try {
            Project p = getOrCreateProject(gitLabApi.getProjectApi());
            String imageUrl = setImageURLProperty(p, gitHost);
            createOrUpdateFile(p, gitLabApi.getRepositoryFileApi(), imageUrl);
        } catch (GitLabApiException ex) {
            throw new MojoExecutionException("Gitlab connection error.", ex);
        }
    }
    
    private String setImageURLProperty(Project p, String gitHost){
        String imageURL = gitHost + ":" + registryPort + "/" + p.getPathWithNamespace() + ":" + projectVersion;
        this.mavenProject.getProperties().setProperty("oaei.imageURL", imageURL);
        return imageURL;
    }
    
    private GitLabApi makeGitLabConnection(String gitHost) throws MojoExecutionException{        
        //first check inline auth
        if(this.authConfig.getPassword() != null){
            getLog().info("Found inline authconfig in pom.xml");
            return checkConnection(this.authConfig.getUsername(), this.authConfig.getPassword());
        }else{
            //second: check settings.xml
            for (Server server : settings.getServers()) {
                String id = server.getId();
                if(id.equals(gitHost + ":" + registryPort)){
                    getLog().info("Found authconfig in settings.xml.");
                    return checkConnection(server.getUsername(), server.getPassword());
                }
            }
            throw new MojoExecutionException("No inline auth config for matching-maven-plugin and no server definition is settings which cooresponds to " + gitHost + ":" + registryPort);
        }
    }
    
    private GitLabApi checkConnection(String user, String password){
        if(user == null || user.trim().isEmpty()){
            //then password needs to be access token
            getLog().info("Use password as access token.");
            return new GitLabApi(this.giturl, password);
        }else{
            //first check: if it is the password of the user
            try {
                GitLabApi api = GitLabApi.oauth2Login(this.giturl, user, password);
                getLog().info("Password was hobbit password.");
                return api;
            } catch (GitLabApiException ex) {
                //second: password needs to be access token
                getLog().info("Password was not hobbit password. Use as access token.");
                return new GitLabApi(this.giturl, password);
            }
        }
    }
    
    private String getHost(String url){        
        URL u = null;
        try {
            u = new URL(url);
        } catch (MalformedURLException ex) {
            getLog().error("cannot parse URL:" + url, ex);
            return url;
        }
        return u.getHost();
    }
    
    private Project getOrCreateProject(ProjectApi projectApi) throws GitLabApiException{
        List<Project> ownedProjects = projectApi.getOwnedProjects();
        for(Project p : ownedProjects){
            if(p.getName().equals(projectArtifactId)){
                getLog().info( "Found project " + p.getName() + " in Hobbit Gitlab. Use it.");
                return p;
            }
        }
        getLog().info("No project found in Hobbit Gitlab. Create a new one with name " + projectArtifactId);
        Project newProject = projectApi.createProject(
                new Project()
                        .withName(projectArtifactId)
                        .withDescription("Auto generated project for matcher " + projectArtifactId)
                        .withVisibility(Visibility.PRIVATE)
                        .withContainerRegistryEnabled(true) //just to be sure
                );
        
        return newProject;
    }
    
    private void createOrUpdateFile(Project project, RepositoryFileApi fileApi, String imageUrl) throws GitLabApiException{
        RepositoryFile f = null;
        try{
            f = fileApi.getFile(project, "system.ttl", defaultbranch);
        }catch(GitLabApiException ex){
            //file not exists - create it
            
            f = new RepositoryFile();
            f.setFilePath("system.ttl");
            f.setContent(getSystemTtlContent(imageUrl));
            getLog().info("The file \"system.ttl\" in the repository does not exist. The file will be created for you.");
            fileApi.createFile(project, f, defaultbranch, "auto generated initial commit for system.ttl");
            return;
        }
        String fileContent = f.getContent();
        if(f.getEncoding() == Encoding.BASE64){
            fileContent = new String(Base64.decodeBase64(fileContent));
        }
        
        fileContent = checkSystemTtlContent(fileContent, imageUrl);
        if(fileContent != null){
            f = new RepositoryFile();
            f.setFilePath("system.ttl");            
            f.setContent(fileContent);
            fileApi.updateFile(project, f, defaultbranch, "auto generated update commit for system.ttl to add a new version.");
        }        
    }
    
    private String checkSystemTtlContent(String content, String imageUrl){
        if(content.contains("sys:" + projectArtifactId + " a hobbit:System ;") == false){
            //not the autogenerated file - override it ?
            getLog().info( "The system.ttl is not the auto generated one. The file is updated with the new content." );
            return getSystemTtlContent(imageUrl);
        }
        if(content.contains("sys:" + projectArtifactId + "-" + projectVersion + " a hobbit:SystemInstance ;")){
            //everthing is fine - the current version exist - nothing to do
            getLog().info( "The system.ttl is fine and will not be modified. Remember to upgrade the version of the matcher in the pom.xml file if you add more benchmarks.");
            return null;
        }
        //current version is missing - add it
        getLog().info( "The system.ttl will be updated to contain the new version of the matcher." );
        content += getSystemInstanceLines(imageUrl);
        return content;
    }
    
    private String getSystemTtlContent(String imageUrl){
        return String.join(NEWLINE, Arrays.asList(
            "@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> .",
            "@prefix hobbit: <http://w3id.org/hobbit/vocab#> .",
            "@prefix sys: <http://w3id.org/system#> .",
            "@prefix bench: <http://w3id.org/bench#> .",
            "",
            "sys:" + projectArtifactId + " a hobbit:System ;",
            "   rdfs:label \"" + projectArtifactId + "\"@en;",
            "   rdfs:comment \"" + projectDescription + "\"@en .",
            "", getSystemInstanceLines(imageUrl)));
    }
    
    private String getSystemInstanceLines(String imageUrl){
        return String.join(NEWLINE, Arrays.asList(
            "sys:" + projectArtifactId + "-" + projectVersion + " a hobbit:SystemInstance ;",
            "   rdfs:label \"" +  projectArtifactId + "-" + projectVersion  + "\"@en;",
            //"   rdfs:comment \"" + projectDescription + "\"@en ;",
            "   hobbit:instanceOf sys:" + projectArtifactId + " ;",
            getImplementsLines(),
            "   hobbit:imageName \"" + imageUrl + "\".",
            "", ""));
    }
    
    private String getImplementsLines(){
        List<String> implementsLines = new ArrayList<>();
        for(String benchmark : benchmarks){
            benchmark = benchmark.trim();
            if(benchmark.startsWith("bench:") || (benchmark.startsWith("<") && benchmark.endsWith(">"))){
                implementsLines.add("   hobbit:implementsAPI " + benchmark + " ;");
            }else{
                implementsLines.add("   hobbit:implementsAPI <" + benchmark + "> ;");
            }
        }
        return String.join(NEWLINE, implementsLines);
    }    

}