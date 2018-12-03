package com.testwa.distest.postman;


import com.testwa.distest.postman.model.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.lang3.StringUtils;

@Slf4j
public class PostmanCollectionRunner {
	public static final String ARG_COLLECTION = "c";
	public static final String ARG_ENVIRONMENT = "e";
	public static final String ARG_FOLDER = "f";
	public static final String ARG_HALTONERROR = "haltonerror";

	private PostmanCollection collection;

	private PostmanEnvironment environment;

	private PostmanVariables sharedPostmanEnvVars;

	public void init(String colFilename, String envFilename) throws Exception {
        log.info("@@@@@ POSTMAN init: {}", colFilename);
        PostmanReader reader = new PostmanReader();
        this.collection = reader.readCollectionFile(colFilename);
        collection.init();
        this.environment = reader.readEnvironmentFile(envFilename);
        environment.init();
    }

	public PostmanRunResult runCollection(String folderId, boolean haltOnError) throws Exception {
		return runCollection(folderId, haltOnError, false);
	}

	/**
	 *
	 * @param folderId
	 * @param haltOnError
	 * @param useSharedPostmanVars
	 *            Use a single set of postman variable(s) across all your tests.
	 *            This allows for running tests between a select few postman
	 *            folders while retaining environment variables between each run
	 * @return
	 */
	public PostmanRunResult runCollection(String folderId, boolean haltOnError, boolean useSharedPostmanVars) {
	    if(collection == null || environment == null) {
	        throw new RuntimeException("请初始化");
        }
        log.info("@@@@@ POSTMAN run start");
		PostmanRunResult runResult = new PostmanRunResult();

		PostmanFolder folder = null;
		if (StringUtils.isNotBlank(folderId)) {
            folder = collection.getFolderLookup().get(folderId);
            if(folder == null) {
                return runResult;
            }
		}

		PostmanVariables var;
		if (useSharedPostmanVars) {
			if (sharedPostmanEnvVars == null) {
				sharedPostmanEnvVars = new PostmanVariables(environment);
			}
			var = sharedPostmanEnvVars;
		} else {
			var = new PostmanVariables(environment);
		}

		PostmanRequestRunner runner = new PostmanRequestRunner(var, haltOnError);
		boolean isSuccessful = true;
		if (folder != null) {
            isSuccessful = folder.run(haltOnError, runner, var, runResult);
		} else {
			// Execute all folder all requests
            isSuccessful = collection.getRootFolder().run(haltOnError, runner, var, runResult);
            if (haltOnError && !isSuccessful) {
                return runResult;
            }
		}

		log.info("@@@@@ Yay! All Done!");
		log.info(runResult.toString());
		return runResult;
	}

    public static void main(String[] args) throws Exception {
        Options options = new Options();
        options.addOption(ARG_COLLECTION, true, "File name of the POSTMAN collection.");
        options.addOption(ARG_ENVIRONMENT, true, "File name of the POSTMAN environment variables.");
        options.addOption(ARG_FOLDER, true,
                "(Optional) POSTMAN collection folder (group) to execute i.environment. \"My Use Cases\"");
        options.addOption(ARG_HALTONERROR, false, "(Optional) Stop on first error in POSTMAN folder.");

        CommandLineParser parser = new BasicParser();
        CommandLine cmd = parser.parse(options, args);
        String colFilename = cmd.getOptionValue(ARG_COLLECTION);
        String envFilename = cmd.getOptionValue(ARG_ENVIRONMENT);
        String folderId = cmd.getOptionValue(ARG_FOLDER);
        boolean haltOnError = cmd.hasOption(ARG_HALTONERROR);

        if (colFilename == null || colFilename.isEmpty() || envFilename == null || envFilename.isEmpty()) {
            // automatically generate the help statement
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("postman-runner", options);
            return;
        }

        PostmanCollectionRunner pcr = new PostmanCollectionRunner();
        pcr.init(colFilename, envFilename);
        pcr.runCollection(folderId, haltOnError, true);
    }
}