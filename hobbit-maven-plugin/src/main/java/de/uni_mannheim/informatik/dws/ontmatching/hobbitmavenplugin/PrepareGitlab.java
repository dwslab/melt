package de.uni_mannheim.informatik.dws.ontmatching.hobbitmavenplugin;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.codec.binary.Base64;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.settings.Server;
import org.apache.maven.settings.Settings;
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
@Mojo( name = "prepareGitlab", defaultPhase = LifecyclePhase.VERIFY)
public class PrepareGitlab extends AbstractMojo{
    private static final String newline = System.getProperty("line.separator");
    
    
    @Parameter( property = "oaei.accesstoken", defaultValue = "")
    private String accesstoken;
    
    @Parameter( property = "oaei.giturl", defaultValue = "https://git.project-hobbit.eu")
    private String giturl;
    
    @Parameter( property = "oaei.registryPort", defaultValue ="4567")
    private String registryPort;
    
    @Parameter( property = "oaei.defaultbranch", defaultValue ="master")
    private String defaultbranch;
    
    @Parameter( property = "oaei.benchmarks", defaultValue ="")
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
        String accessToken = getAccessToken(gitHost);
        if(accessToken.length() == 0){
            throw new MojoExecutionException("Find no accessToken in pom or settings.xml.");
        }
        GitLabApi gitLabApi = new GitLabApi(giturl, accessToken);
        try {
            Project p = getOrCreateProject(gitLabApi.getProjectApi());
            String imageUrl = setImageURLProperty(p, gitHost);
            createOrUpdateFile(p, gitLabApi.getRepositoryFileApi(), imageUrl);
        } catch (GitLabApiException ex) {
            throw new MojoExecutionException("Gitlab connection error", ex);
        }
    }
    
    private String setImageURLProperty(Project p, String gitHost){
        String imageURL = gitHost + ":" + registryPort + "/" + p.getPathWithNamespace() + ":" + projectVersion;
        this.mavenProject.getProperties().setProperty("oaei.imageURL", imageURL);
        return imageURL;
    }
    
    
    private String getAccessToken(String gitHost){
        //check own config section
        if(this.accesstoken != null && this.accesstoken.trim().length() > 0){
            return this.accesstoken.trim();
        }
        //check settings.xml
        for (Server server : settings.getServers()) {
            String id = server.getId();
            if(id.equals(gitHost)){
                return server.getPrivateKey();
            }
        }
        return "";
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
                return p;
            }
        }
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
            f = fileApi.getFile("system.ttl", project.getId(), defaultbranch);
        }catch(GitLabApiException ex){
            //file not exists - create it
            f = new RepositoryFile();
            f.setFilePath("system.ttl");
            f.setContent(getSystemTtlContent(imageUrl));
            getLog().info( "create system.ttl file on gitlab server" );
            fileApi.createFile(f, project.getId(), defaultbranch, "auto generated initial commit for system.ttl");
            return;
        }
        String fileContent = f.getContent();
        if(f.getEncoding().toLowerCase().equals("base64")){
            fileContent = new String(Base64.decodeBase64(fileContent));
        }
        
        fileContent = checkSystemTtlContent(fileContent, imageUrl);
        if(fileContent != null){
            f = new RepositoryFile();
            f.setFilePath("system.ttl");            
            f.setContent(fileContent);
            fileApi.updateFile(f, project.getId(), defaultbranch, "auto generated update commit for system.ttl to add a new version.");
        }        
    }
    
    private String checkSystemTtlContent(String content, String imageUrl){
        if(content.contains("sys:" + projectArtifactId + " a hobbit:System ;") == false){
            //not the autogenerated file - override it ?
            getLog().info( "replace system.ttl file on gitlab server because current is not correct" );
            return getSystemTtlContent(imageUrl);
        }
        if(content.contains("sys:" + projectArtifactId + "-" + projectVersion + " a hobbit:SystemInstance ;")){
            //everthing is fine - the current version exist - nothing to do
            getLog().info( "system.ttl file on gitlab server is fine");
            return null;
        }
        //current version is missing - add it
        getLog().info( "update system.ttl file on gitlab server to add a new version" );
        content += getSystemInstanceLines(imageUrl);
        return content;
    }
    
    private String getSystemTtlContent(String imageUrl){
        return String.join(newline, Arrays.asList(
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
        return String.join(newline, Arrays.asList(
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
        return String.join(newline, implementsLines);
    }    
}