package com.rsv.rsvimport;

import java.util.Arrays;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class Arguments {

    private Log log = LogFactory.getLog(getClass());
    private String serverip = "localhost";
    private String serverport = "";
    private String ticket;
    private static Arguments arg = null;
    private String username = null;
    private String passwd = null;
    private String protocol = "http://";
    private List<String> arguments = null;
    private String tasktype = "IN Progress";
    private String location = System.getProperty("user.home");
    private String usermail = "icptest@rsageventures.com";
    private String trialname = null;
    private String taskusers = null;
    private String taskid = null;
    private String xmlfile = null;
    private String trialid = null;
    private boolean completedtasks = false;
    private String taskname = null;
    private boolean removeold = false;
    private int level = 0;
    private boolean incrementalorder = false;
    private int port = 443;
    private String parent = null;
    private String configFile;
    private int threads = 1;
    private boolean senddtf = false;
    private boolean dcmmodify = true;

    private String transferSyntaxUID = null;
    private boolean compressPixelData = false;
    private boolean decompressPixelData = false;

    public static Arguments getInstance() {
        if (arg == null) {
            arg = new Arguments();
        }
        return arg;
    }

    public void parseArguments(String[] args) {
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("--empty--")) {
                args[i] = "";
            }
        }
        arguments = Arrays.asList(args);

        if (arguments.contains("--help")) {
            log.info("Application Usage Options:\n\t"
                    + "-ip <SERVER IP ADDRESS> (DEFAULT localhost)\n\t"
                    + "-port <SERVER PORT (DEFAULT 8080)\n\t"
                    + "-ticket <AUTHENTICATION TICKET>\n\t"
                    + "-user <USER NAME>\n\t"
                    + "-password <PASSWORD>\n\t"
                    + "-tasktype <Task Type>\n\t"
                    + "-tsuid <Transfer Syntax UID>\n\t"
                    + "-compress <true|false>\n\t"
                    + "-decompress <true|false>\n\t"
                    + "--help <for help>");
            System.exit(1);
        }

        setIfExists("-ip", this::setServerip);
        setIfExists("-port", this::setServerport);
        setIfExists("-ticket", this::setTicket);
        setIfExists("-user", this::setUsername);
        setIfExists("-password", this::setPasswd);
        setIfExists("-protocol", v -> setProtocol(v + "://"));
        setIfExists("-tasktype", this::setTasktype);
        setIfExists("-location", this::setLocation);
        setIfExists("-mail", this::setUsermail);
        setIfExists("-trialname", this::setTrialname);
        setIfExists("-taskusers", this::setTaskusers);
        setIfExists("-taskid", this::setTaskid);
        setIfExists("-xmlfile", this::setXmlfile);
        setIfExists("-trialid", this::setTrialid);
        setIfExists("-taskname", this::setTaskname);
        setIfExists("-parent", this::setParent);
        setIfExists("-configfile", this::setConfigFile);
        setIfExists("-threads", v -> setThreads(Integer.parseInt(v)));
        setIfExists("-level", v -> setLevel(Integer.parseInt(v)));
        setIfExists("-port", v -> setPort(Integer.parseInt(v)));

        setIfExists("-completedtasks", v -> setCompletedtasks(Boolean.parseBoolean(v)));
        setIfExists("-removeold", v -> setRemoveold(Boolean.parseBoolean(v)));
        setIfExists("-incrementalorder", v -> setIncrementalorder(Boolean.parseBoolean(v)));
        setIfExists("-senddtf", v -> setSenddtf(Boolean.parseBoolean(v)));
        setIfExists("-dcmmodify", v -> setDcmmodify(Boolean.parseBoolean(v)));

        // dcm4che5-specific options
        setIfExists("-tsuid", this::setTransferSyntaxUID);
        setIfExists("-compress", v -> setCompressPixelData(Boolean.parseBoolean(v)));
        setIfExists("-decompress", v -> setDecompressPixelData(Boolean.parseBoolean(v)));
    }

    private void setIfExists(String flag, java.util.function.Consumer<String> setter) {
        int index = arguments.indexOf(flag);
        if (index != -1 && index + 1 < arguments.size()) {
            setter.accept(arguments.get(index + 1));
        }
    }

   
    public Log getLog() { return log; }
    public void setLog(Log log) { this.log = log; }
    public String getServerip() { return serverip; }
    public void setServerip(String serverip) { this.serverip = serverip; }
    public String getServerport() { return serverport; }
    public void setServerport(String serverport) { this.serverport = serverport; }
    public String getTicket() { return ticket; }
    public void setTicket(String ticket) { this.ticket = ticket; }
    public static Arguments getArg() { return arg; }
    public static void setArg(Arguments arg) { Arguments.arg = arg; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPasswd() { return passwd; }
    public void setPasswd(String passwd) { this.passwd = passwd; }
    public String getProtocol() { return protocol; }
    public void setProtocol(String protocol) { this.protocol = protocol; }
    public List<String> getArguments() { return arguments; }
    public void setArguments(List<String> arguments) { this.arguments = arguments; }
    public String getTasktype() { return tasktype; }
    public void setTasktype(String tasktype) { this.tasktype = tasktype; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public String getUsermail() { return usermail; }
    public void setUsermail(String usermail) { this.usermail = usermail; }
    public String getTrialname() { return trialname; }
    public void setTrialname(String trialname) { this.trialname = trialname; }
    public String getTaskusers() { return taskusers; }
    public void setTaskusers(String taskusers) { this.taskusers = taskusers; }
    public String getTaskid() { return taskid; }
    public void setTaskid(String taskid) { this.taskid = taskid; }
    public String getXmlfile() { return xmlfile; }
    public void setXmlfile(String xmlfile) { this.xmlfile = xmlfile; }
    public String getTrialid() { return trialid; }
    public void setTrialid(String trialid) { this.trialid = trialid; }
    public boolean isCompletedtasks() { return completedtasks; }
    public void setCompletedtasks(boolean completedtasks) { this.completedtasks = completedtasks; }
    public String getTaskname() { return taskname; }
    public void setTaskname(String taskname) { this.taskname = taskname; }
    public boolean getRemoveold() { return removeold; }
    public void setRemoveold(boolean removeold) { this.removeold = removeold; }
    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = level; }
    public boolean isIncrementalorder() { return incrementalorder; }
    public void setIncrementalorder(boolean incrementalorder) { this.incrementalorder = incrementalorder; }
    public int getPort() { return port; }
    public void setPort(int port) { this.port = port; }
    public String getParent() { return parent; }
    public void setParent(String parent) { this.parent = parent; }
    public String getConfigFile() { return configFile; }
    public void setConfigFile(String configFile) { this.configFile = configFile; }
    public int getThreads() { return threads; }
    public void setThreads(int threads) { this.threads = threads; }
    public boolean isSenddtf() { return senddtf; }
    public void setSenddtf(boolean senddtf) { this.senddtf = senddtf; }
    public boolean isDcmmodify() { return dcmmodify; }
    public void setDcmmodify(boolean dcmmodify) { this.dcmmodify = dcmmodify; }

    public String getTransferSyntaxUID() { return transferSyntaxUID; }
    public void setTransferSyntaxUID(String transferSyntaxUID) { this.transferSyntaxUID = transferSyntaxUID; }
    public boolean isCompressPixelData() { return compressPixelData; }
    public void setCompressPixelData(boolean compressPixelData) { this.compressPixelData = compressPixelData; }
    public boolean isDecompressPixelData() { return decompressPixelData; }
    public void setDecompressPixelData(boolean decompressPixelData) { this.decompressPixelData = decompressPixelData; }
}
