package org.hivedb.teamcity.plugin;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.StringReader;
import java.util.Collection;
import java.util.ArrayList;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.text.ParseException;


public class Git {
  String gitCommand;
  public static final String GIT_DATE_FORMAT = "EEE MMM dd HH:mm:ss yyyy Z";

  public Git(String cmd) {
    this.gitCommand = cmd;
  }

  public Collection<Commit> log(int n) {
    String log = runCommand(String.format("%s log -n %s", getGitCommand(), n));
    return parseCommitLog(log);
  }



  private String runCommand(String cmd) {
    Process cmdProc = null;
    try {
      cmdProc = Runtime.getRuntime().exec(cmd);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    BufferedReader in = new BufferedReader(new InputStreamReader(cmdProc.getInputStream()));
    StringBuilder output = new StringBuilder();
    String line;
    try {
      while((line = in.readLine()) != null) {
        output.append(line);
        output.append("\n");
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    return output.toString();
  }

  public String getGitCommand() {
    return gitCommand;
  }

  private Collection<Commit> parseCommitLog(String log) {
    Collection<Commit> commits = new ArrayList<Commit>();
    BufferedReader r = new BufferedReader(new StringReader(log));
    String line;
    try {
      Commit current = new Commit();
      while((line = r.readLine()) != null) {
        String s = line.trim();
        if( s == null || "".equals(s))
          continue;
        else {
          if(s.startsWith("commit")) {
            if(current.isValid()) {
              commits.add(current);
              current = new Commit();
            }
            current.setId(s.replaceAll("commit", "").trim());
          } else if(s.startsWith("Author"))
            current.setAuthor(s.replaceAll("Author:","").trim());
          else if(s.startsWith("Date")){
            String dateString = s.replaceAll("Date:","").trim();
            SimpleDateFormat format = new SimpleDateFormat(GIT_DATE_FORMAT);
            current.setDate(format.parse(dateString));
          } else {
            current.setMessage(s);
            commits.add(current);
            current = new Commit();
          }
        }
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    } catch (ParseException e) {
      throw new RuntimeException(e);
    }
    return commits;
  }
}
