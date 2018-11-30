package com.testwa.distest.postman;


import com.testwa.distest.postman.model.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;

@Slf4j
public class PostmanCollectionRunner {
	public static final String ARG_COLLECTION = "c";
	public static final String ARG_ENVIRONMENT = "e";
	public static final String ARG_FOLDER = "f";
	public static final String ARG_HALTONERROR = "haltonerror";

	private PostmanVariables sharedPostmanEnvVars;

	public static void main(String[] args) throws Exception {
		Options options = new Options();
		options.addOption(ARG_COLLECTION, true, "File name of the POSTMAN collection.");
		options.addOption(ARG_ENVIRONMENT, true, "File name of the POSTMAN environment variables.");
		options.addOption(ARG_FOLDER, true,
				"(Optional) POSTMAN collection folder (group) to execute i.e. \"My Use Cases\"");
		options.addOption(ARG_HALTONERROR, false, "(Optional) Stop on first error in POSTMAN folder.");

		CommandLineParser parser = new BasicParser();
		CommandLine cmd = parser.parse(options, args);
		String colFilename = cmd.getOptionValue(ARG_COLLECTION);
		String envFilename = cmd.getOptionValue(ARG_ENVIRONMENT);
		String folderName = cmd.getOptionValue(ARG_FOLDER);
		boolean haltOnError = cmd.hasOption(ARG_HALTONERROR);

		if (colFilename == null || colFilename.isEmpty() || envFilename == null || envFilename.isEmpty()) {
			// automatically generate the help statement
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("postman-runner", options);
			return;
		}

		PostmanCollectionRunner pcr = new PostmanCollectionRunner();
		pcr.runCollection(colFilename, envFilename, folderName, haltOnError, false);
	}

	public PostmanRunResult runCollection(String colFilename, String envFilename, String folderName,
										  boolean haltOnError) throws Exception {
		return runCollection(colFilename, envFilename, folderName, haltOnError, false);
	}

	/**
	 *
	 * @param colFilename
	 * @param envFilename
	 * @param folderName
	 * @param haltOnError
	 * @param useSharedPostmanVars
	 *            Use a single set of postman variable(s) across all your tests.
	 *            This allows for running tests between a select few postman
	 *            folders while retaining environment variables between each run
	 * @return
	 * @throws Exception
	 */
	public PostmanRunResult runCollection(String colFilename, String envFilename, String folderName,
										  boolean haltOnError, boolean useSharedPostmanVars) throws Exception {
		log.info("@@@@@ POSTMAN Runner start: {}", colFilename);
		PostmanRunResult runResult = new PostmanRunResult();

		PostmanReader reader = new PostmanReader();
		PostmanCollection c = reader.readCollectionFile(colFilename);
		c.init();
		PostmanEnvironment e = reader.readEnvironmentFile(envFilename);
		e.init();
		PostmanFolder folder = null;
		if (folderName != null && !folderName.isEmpty()) {
			folder = c.getFolderLookup().get(folderName);
		}

		PostmanVariables var;
		if (useSharedPostmanVars) {
			if (sharedPostmanEnvVars == null) {
				sharedPostmanEnvVars = new PostmanVariables(e);
			}
			var = sharedPostmanEnvVars;
		} else {
			var = new PostmanVariables(e);
		}

		PostmanRequestRunner runner = new PostmanRequestRunner(var, haltOnError);
		boolean isSuccessful = true;
		if (folder != null) {
			isSuccessful = runFolder(haltOnError, runner, var, folder, runResult);
		} else {
			// Execute all folder all requests
			for (PostmanFolder pf : c.getItem()) {
				isSuccessful = runFolder(haltOnError, runner, var, pf, runResult) && isSuccessful;
				if (haltOnError && !isSuccessful) {
					return runResult;
				}
			}
		}

		log.info("@@@@@ Yay! All Done!");
		log.info(runResult.toString());
		return runResult;
	}

	private boolean runFolder(boolean haltOnError, PostmanRequestRunner runner, PostmanVariables var,
							  PostmanFolder folder, PostmanRunResult runResult) {
		log.info("==> POSTMAN Folder: " + folder.getName());
		boolean isSuccessful = true;
		for (PostmanItem fItem : folder.getItem()) {
			runResult.totalRequest++;
			log.info("======> POSTMAN request: " + fItem.getName());
			try {
				boolean runSuccess = runner.run(fItem, runResult);
				if (!runSuccess) {
					runResult.failedRequest++;
					runResult.failedRequestName.add(folder.getName() + "." + fItem.getName());
				}
				isSuccessful = runSuccess && isSuccessful;
				if (haltOnError && !isSuccessful) {
					return isSuccessful;
				}
			} catch (Throwable e) {
				e.printStackTrace();
				runResult.failedRequest++;
				runResult.failedRequestName.add(folder.getName() + "." + fItem.getName());
				return false;
			}
		}
		return isSuccessful;
	}
}