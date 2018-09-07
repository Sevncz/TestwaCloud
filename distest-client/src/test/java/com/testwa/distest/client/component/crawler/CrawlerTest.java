package com.testwa.distest.client.component.crawler;


import com.testwa.core.shell.UTF8CommonExecs;
import com.testwa.distest.client.DistestClientApplication;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.exec.CommandLine;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;

import static org.apache.commons.exec.ExecuteWatchdog.INFINITE_TIMEOUT;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(classes = DistestClientApplication.class)
@TestPropertySource(locations="classpath:application-test.properties")
public class CrawlerTest {


    @Test
    public void testCrawlerStart() {
        String cralwerJarPath = "/Applications/Testwa Agent.app/Contents/Resources/static/java/resources/crawler/UICrawler-2.0.jar".replaceAll("\\s+", "\" \"");
        CommandLine commandLine = new CommandLine("java");
        commandLine.addArgument("-jar");
        commandLine.addArgument(cralwerJarPath);

        UTF8CommonExecs javaExecs = new UTF8CommonExecs(commandLine);
        try {
            javaExecs.setTimeout(INFINITE_TIMEOUT);
            javaExecs.exec();
            String output = javaExecs.getOutput();
            System.out.println(output);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
