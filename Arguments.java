package com.rsv.rsvimport;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 
 * @author Chaitu
 */
public class Arguments {

	private Log log = LogFactory.getLog(getClass());
	private String serverip = "localhost";
	private String serverport = "";
	private String ticket;
	// public static ResourceBundle resourceBundle;
	private static Arguments arg = null;
	private String username = null;
	private String passwd = null;
	private String  protocol="http://";
	private List<String> arguments = null;
	private String  tasktype="IN Progress";
	private String  location=System.getProperty("user.home");
	private String usermail="icptest@rsageventures.com";
	private String trialname=null;
	private String taskusers=null;
	private String taskid=null;
	private String xmlfile=null;
	private String trialid=null;
	private boolean completedtasks=false;
	private String taskname=null;
	private boolean removeold=false;
	private int level=0;
	private boolean incrementalorder=false;
	private int port=443;
	private String parent=null;
        private String configFile;
        private int threads=1;
        private boolean senddtf=false;
        private boolean dcmmodify=true;

    public boolean isDcmmodify() {
        return dcmmodify;
    }

    public void setDcmmodify(boolean dcmmodify) {
        this.dcmmodify = dcmmodify;
    }

        public boolean isSenddtf() {
            return senddtf;
        }

        public void setSenddtf(boolean senddtf) {
            this.senddtf = senddtf;
        }

        

          public int getThreads() {
		return threads;
	}

	public void setThreads(int threads) {
		this.threads = threads;
	}

	
	

	public String getParent() {
		return parent;
	}

	public void setParent(String parent) {
		this.parent = parent;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}
	
	

	public boolean isIncrementalorder() {
		return incrementalorder;
	}

	public void setIncrementalorder(boolean incrementalorder) {
		this.incrementalorder = incrementalorder;
	}

	public boolean getRemoveold() {
		return removeold;
	}

	public void setRemoveold(boolean removeold) {
		this.removeold = removeold;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public String getXmlfile() {
		return xmlfile;
	}

	public void setXmlfile(String xmlfile) {
		this.xmlfile = xmlfile;
	}

	public String getTaskid() {
		return taskid;
	}

	public void setTaskid(String taskid) {
		this.taskid = taskid;
	}

	public String getUsermail() {
		return usermail;
	}

	public void setUsermail(String usermail) {
		this.usermail = usermail;
	}

	public String getTasktype() {
		return tasktype;
	}

	public void setTasktype(String tasktype) {
		this.tasktype = tasktype;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public Log getLog() {
		return log;
	}

	public void setLog(Log log) {
		this.log = log;
	}

	public String getServerip() {
		return serverip;
	}

	public void setServerip(String serverip) {
		this.serverip = serverip;
	}

	public String getServerport() {
		return serverport;
	}

	public void setServerport(String serverport) {
		this.serverport = serverport;
	}

	public String getTicket() {
		return ticket;
	}

	public void setTicket(String ticket) {
		this.ticket = ticket;
	}

	public static Arguments getArg() {
		return arg;
	}

	public static void setArg(Arguments arg) {
		Arguments.arg = arg;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPasswd() {
		return passwd;
	}

	public void setPasswd(String passwd) {
		this.passwd = passwd;
	}

	public String getProtocol() {
		return protocol;
	}

	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}

	public List<String> getArguments() {
		return arguments;
	}

	public void setArguments(List<String> arguments) {
		this.arguments = arguments;
	}

	public static Arguments getInstance() {
		if (arg == null) {
			arg = new Arguments();
		}
		return arg;
	}

	public void parseArguments(String[] args) {
		for(int i=0;i<args.length;i++){
			if(args[i].equals("--empty--")){
				args[i] = "";
			}
		}
		arguments = Arrays.asList(args);
		int index = arguments.indexOf("--help");
		if (index != -1) {
			log
					.info("Application Usage Options:\n\t"
							+ "-ip <SERVER IP ADDRESS> (DEFAULT localhost)\n\t"
							+ "-port <SERVER PORT (DEFAULT 8080)\n\t"
							+ "-ticket <AUTHENTICATION TICKET FROM ALFRESCO>\n\t"
							+ "-user <USER NAME>\n\t"
							+ "--help <for help >");

			System.exit(1);

		}
		index = arguments.indexOf("-ip");
		if (index != -1) {
			setServerip(arguments.get(index + 1));

		}

		index = arguments.indexOf("-port");
		if (index != -1) {
			setServerport(arguments.get(index + 1));

		}
		index = arguments.indexOf("-ticket");
		if (index != -1) {
			setTicket(arguments.get(index + 1));

		}
		index = arguments.indexOf("-user");
		if (index != -1) {
			setUsername(arguments.get(index + 1));

		}
		index = arguments.indexOf("-password");
		if (index != -1) {
			setPasswd(arguments.get(index + 1));

		}
		index = arguments.indexOf("-protocol");
		if (index != -1) {
			protocol=arguments.get(index + 1)+"://";
			

		}
		index = arguments.indexOf("-tasktype");
		if (index != -1) {
			setTasktype(arguments.get(index + 1));

		}
		
		index = arguments.indexOf("-location");
		if (index != -1) {
			setLocation(arguments.get(index + 1));

		}
		index = arguments.indexOf("-mail");
		if (index != -1) {
			setUsermail(arguments.get(index + 1));

		}
		index = arguments.indexOf("-trialname");
		if (index != -1) {
			setTrialname(arguments.get(index + 1));

		}
		index = arguments.indexOf("-taskusers");
		if (index != -1) {
			setTaskusers(arguments.get(index + 1));

		}
		index = arguments.indexOf("-taskid");
		if (index != -1) {
			setTaskid(arguments.get(index + 1));

		}
		index = arguments.indexOf("-xmlfile");
		if (index != -1) {
			setXmlfile(arguments.get(index + 1));

		}
		index = arguments.indexOf("-trialid");
		if (index != -1) {
			setTrialid(arguments.get(index + 1));

		}
		index = arguments.indexOf("-completedtasks");
		if (index != -1) {
			setCompletedtasks(Boolean.parseBoolean(arguments.get(index + 1)));

		}
		index = arguments.indexOf("-taskname");
		if (index != -1) {
			setTaskname(arguments.get(index + 1));

		}
		index = arguments.indexOf("-removeold");
		if (index != -1) {
			setRemoveold(Boolean.parseBoolean(arguments.get(index + 1)));

		}
		index = arguments.indexOf("-level");
		if (index != -1) {
			setLevel(Integer.parseInt(arguments.get(index + 1)));

		}
		index = arguments.indexOf("-incrementalorder");
		if (index != -1) {
			setIncrementalorder(Boolean.parseBoolean(arguments.get(index + 1)));
		}
		
		index = arguments.indexOf("-port");
		if (index != -1) {
			setPort(Integer.parseInt(arguments.get(index + 1)));
		}
		index = arguments.indexOf("-parent");
		if (index != -1) {
			setParent(arguments.get(index + 1));

		}
                
                index = arguments.indexOf("-configfile");
		if (index != -1) {
			setConfigFile(arguments.get(index + 1));

		}

		index = arguments.indexOf("-senddtf");
		if (index != -1) {
			setSenddtf(Boolean.parseBoolean(arguments.get(index + 1)));

		}
                
		index = arguments.indexOf("-dcmmodify");
		if (index != -1) {
	         setDcmmodify(Boolean.parseBoolean(arguments.get(index + 1)));

		}
		
	}

	public String getTrialname() {
		return trialname;
	}

	public void setTrialname(String trialname) {
		this.trialname = trialname;
	}

	public String getTaskusers() {
		return taskusers;
	}

	public void setTaskusers(String taskusers) {
		this.taskusers = taskusers;
	}

	public String getTrialid() {
		return trialid;
	}

	public void setTrialid(String trialid) {
		this.trialid = trialid;
	}

	public boolean isCompletedtasks() {
		return completedtasks;
	}

	public void setCompletedtasks(boolean completedtasks) {
		this.completedtasks = completedtasks;
	}

	public String getTaskname() {
		return taskname;
	}

	public void setTaskname(String taskname) {
		this.taskname = taskname;
	}
        
        public String getConfigFile()
        {
            return this.configFile;
        }
        public void setConfigFile(String configFile)
        {
            this.configFile=configFile;
        }

	

}
