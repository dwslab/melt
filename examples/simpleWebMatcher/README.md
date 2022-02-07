This is an exaple for a simple web matcher.
In case more software than vanilla java is necessary you can use a different base image and commands to customize your docker image.

As an example, you can use the Ubuntu base image and install all software you need via additional run commands (e.g. python etc):

```
<images>
    <image>
        <name>%a-%v-web</name>
        <build>
            <from>ubuntu:20.04</from>
            <runCmds><!--ampersand (&) unfortunately needs to be encoded as &amp; in the run description-->
                <run>
                    apt update &amp;&amp; \
                        apt install -y --no-install-recommends --no-install-suggests default-jre-headless libc-dev build-essential python3-dev python3-pip python-is-python3 &amp;&amp; \
                        pip install gensim==3.8.3 flask>=1.1.1 numpy>=1.11.3 scikit-learn>=0.23.1 pandas>=1.1.0 torch transformers &amp;&amp; \
                        apt remove -y python3-pip &amp;&amp; \
                        apt autoremove &amp;&amp; \
                        apt clean &amp;&amp; \
                        rm -rf /var/lib/apt/lists/*
                </run>
            </runCmds>
            <assembly><descriptorRef>web</descriptorRef></assembly>
            <cmd><shell>java -cp "${project.build.finalName}.${project.packaging}:lib/*" de.uni_mannheim.informatik.dws.melt.receiver_http.Main</shell></cmd>
            <workdir>/maven</workdir>
            <ports><port>8080</port></ports><!--port exposure to specify on which port the server runs -->
        </build>
    </image>
</images>
```

In the above example it is important to have no whitespace after the backslash `\` within the `<run>` elements.

It is also possible to have multiple `run` elements and set `optimize` to `true` such that no multiple layers are created.
This helps also in reducing the overall image size.

```
<images>
    <image>
        <name>%a-%v-web</name>
        <build>
            <from>ubuntu:20.04</from>
            <runCmds>
                <run>apt update</run>
                <run>apt install -y --no-install-recommends --no-install-suggests default-jre-headless libc-dev build-essential python3-dev python3-pip python-is-python3</run>
                <run>pip install gensim==3.8.3 flask>=1.1.1 numpy>=1.11.3 scikit-learn>=0.23.1 pandas>=1.1.0 torch transformers</run>
                <run>apt remove -y python3-pip</run>
                <run>apt autoremove</run>
                <run>apt clean</run>
                <run>rm -rf /var/lib/apt/lists/*</run>
            </runCmds>
            <optimise>true</optimise><!--important to reduce the size of the image by concat all run commands together -->
            <assembly><descriptorRef>web</descriptorRef></assembly>
            <cmd><shell>java -cp "${project.build.finalName}.${project.packaging}:lib/*" de.uni_mannheim.informatik.dws.melt.receiver_http.Main</shell></cmd>
            <workdir>/maven</workdir>
            <ports><port>8080</port></ports><!--port exposure to specify on which port the server runs -->
        </build>
    </image>
</images>
```