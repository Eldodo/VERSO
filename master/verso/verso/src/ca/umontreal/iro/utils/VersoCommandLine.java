package ca.umontreal.iro.utils;

import java.io.File;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;


/**
 * Description: Wrapper over CommandLine package for VErso usage.
 *
 * @author Edouard Batot
 * @since 1.8.0_131
 */
public class VersoCommandLine {

	private CommandLine commandLine;
    private Options options = new Options();
    private HelpFormatter helpFormatter = new HelpFormatter();
    private boolean hasNoArgs = false;
    
    
	private static final String O_SOFTWARE = "s";
	private static final String O_SOFTWARE_LONG = "software";
	private static final String O_SOFTWARE_DEFAULT = "SweetHome";
	
	private static final String O_FORCELAYOUT = "fl";
	private static final String O_FORCELAYOUT_LONG = "force-layout";
    
	private static final String O_IR_REPO = "ir";
	private static final String O_IR_REPO_LONG = "ir-forlder";
	private static final String O_IR_REPO_DEFAULT = "verso"+File.separator+"irfolder";
    
	private static final String O_MISC_REPO = "misc";
	private static final String O_MISC_REPO_LONG = "misc-folder";
	private static final String O_MISC_REPO_DEFAULT = "verso"+File.separator+"data"+File.separator+"Data_VERSO_HEB";
	

    /**
     * Load Config file before instantiation.<br>
     * @param args
     * @throws ParseException
     */
    public VersoCommandLine(String[] args)  {

        if (args.length == 0) {
            this.hasNoArgs = true;
        }

        Option softOption = Option.builder(O_SOFTWARE)
                .longOpt(O_SOFTWARE_LONG)
                .hasArg()
                .numberOfArgs(1)
                .argName("Software name")
                .desc("Software under study. Default: "+O_SOFTWARE_DEFAULT)
                .build();
        
        Option irfolderOption = Option.builder(O_IR_REPO)
                .longOpt(O_IR_REPO_LONG)
                .hasArg()
                .numberOfArgs(1)
                .argName("Information retrieval interface folder.")
                .desc("Information retrieval interface folder. Default: "+O_IR_REPO_DEFAULT)
                .build();
        
        Option miscfolderOption = Option.builder(O_MISC_REPO)
                .longOpt(O_MISC_REPO_LONG)
                .hasArg()
                .numberOfArgs(1)
                .argName("Information retrieval interface folder.")
                .desc("Information retrieval interface folder. Default: "+O_MISC_REPO_DEFAULT)
                .build();

        Option forceLayoutOption = Option.builder(O_FORCELAYOUT)
                .longOpt(O_FORCELAYOUT_LONG)
                .argName("Boolean")
                .desc("If ON, force layout (and OVERWRITE existing .layout files")
                .build();


        Option helpOption = Option.builder("h")
                .longOpt("help")
                .desc("Show this help menu")
                .build();


        options.addOption(miscfolderOption);
        options.addOption(irfolderOption);
        options.addOption(softOption);
        options.addOption(forceLayoutOption);
        options.addOption(helpOption);

        try {
            this.commandLine = new DefaultParser().parse(options, args);
        } catch (Exception e) {
            System.out.println("Command Line Parameter Problem: "+e.getMessage());
            printHelp();
            System.exit(1);
        }
        

        if (hasOption("h")) {
            printHelp();
            System.exit(0);
        }
    }

    boolean hasOption(String opt) {
        return this.commandLine.hasOption(opt);
    }

    void printHelp() {
        this.helpFormatter.printHelp("VErso", this.options);
    }

    boolean hasNoArgs() {
        return this.hasNoArgs;
    }


	public double getOption(String optName, double defaut) {
		if (this.commandLine.hasOption(optName)) {
			return Integer.parseInt(this.commandLine.getOptionValue(optName));
		} else {
			return defaut;
		}
	}

	public String getOption(String optName, String defaut) {
		if (this.commandLine.hasOption(optName)) {
			return this.commandLine.getOptionValue(optName);
		} else {
			return defaut;
		}
	}

	public String getSoftware() {
		return (String) getOption(O_SOFTWARE, O_SOFTWARE_DEFAULT);
	}
	
	public String getIRFolder() {
		return (String) getOption(O_IR_REPO, O_IR_REPO_DEFAULT);
	}
	
	public String getMiscFolder() {
		return (String) getOption(O_MISC_REPO, O_MISC_REPO_DEFAULT);
	}

	public boolean getForceLayout() {
		return hasOption(O_FORCELAYOUT);
	}

}
