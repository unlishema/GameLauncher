import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;

import javax.swing.JProgressBar;

import processing.core.PApplet;
import processing.data.JSONArray;
import processing.data.JSONObject;

public class VersionFile {

  private final PApplet app;
  public final Version version;

  private final ArrayList<FileData> files = new ArrayList<FileData>();

  public VersionFile(final PApplet app, final Version version) {
    this.app = app;
    this.version = version;
  }

  public VersionFile(final PApplet app, final File file) {
    this.app = app;
    final String s = file.toString();
    final String versionString = s.substring(s.lastIndexOf("\\") + 1, s.lastIndexOf("."));
    this.version = new Version(versionString);
    this.loadData(file.getAbsolutePath());
  }

  public VersionFile(final PApplet app, final Version version, final String fileURL) {
    this.app = app;
    this.version = version;
    this.loadData(fileURL);
  }

  private void loadData(final String location) {
    try {
      final JSONObject obj = this.app.loadJSONObject(location);
      final JSONArray fileData = obj.getJSONArray("files");

      for (int i = 0; i < fileData.size(); i++) {
        final JSONObject jo = fileData.getJSONObject(i);
        final String action = jo.getString("action");
        final File f = new File(jo.getString("file"));
        final long lm = jo.getLong("lastModified");

        final FileData fd = this.addFile(action, f, lm);

        if (Version.debugDetail) System.out.println("FileData: " + fd);
      }
    } 
    catch (final Exception e) {
      System.err.println(e);
    }
  }

  public FileData addFile(final String action, final File file) {
    final FileData fd = new FileData(action, file);
    this.files.add(fd);
    return fd;
  }

  public FileData addFile(final String action, final File file, final long lastModified) {
    final FileData fd = new FileData(action, file, lastModified);
    this.files.add(fd);
    return fd;
  }

  private void copyFileUsingStream(final File source, final File dest) throws IOException {
    InputStream is = null;
    OutputStream os = null;
    try {
      is = new FileInputStream(source);
      os = new FileOutputStream(dest);
      byte[] buffer = new byte[1024];
      int length;
      while ((length = is.read(buffer)) > 0) {
        os.write(buffer, 0, length);
      }
    } 
    catch (final Exception e) {
      System.err.println(e);
    } 
    finally {
      if (is != null) is.close();
      if (os != null) os.close();
    }
  }

  public void copyUpdatedFiles(final JProgressBar progressBar, final File copyDir) {
    if (copyDir.exists() && copyDir.delete()) {
      if (Version.debugDetail) System.out.println("Deleted Dir: " + copyDir);
    }
    if (!copyDir.exists()) copyDir.mkdirs();

    if (Version.debug) System.out.println("Copying " + this.files.size() + " Files!");

    int i = 0;
    for (final FileData fd : this.files) {
      if (fd.getAction().equals(FileData.CREATE) || fd.getAction().equals(FileData.MODIFY)) {
        if (!fd.getFile().isDirectory()) {
          final File file = fd.getFile();
          final String fileName = file.getName();
          final String filePath = file.getPath().replace(fileName, "");

          final File copyFile = new File(copyDir.getPath() + File.separator + filePath, fileName);

          try {
            if (!copyFile.exists()) {
              final File dirFile = new File(copyDir.getPath() + File.separator + filePath);
              dirFile.mkdirs();
              copyFile.createNewFile();
            }
            this.copyFileUsingStream(file, copyFile);
            if (Version.debugDetail) System.out.println("Copied File: " + copyFile);
          } 
          catch (Exception e) {
            System.err.println(e);
          }
        }

        double progress = (((double) i) / ((double) this.files.size())) * 100;
        progressBar.setValue((int) progress);
      }
      i++;
    }
    if (Version.debug) System.out.println("Copied " + this.files.size() + " Files!");
    if (Version.debugDetail) System.out.println();
  }

  public FileData getFile(final File file) {
    for (final FileData f : this.files) {
      final String fileString = file.toString();
      final String testFile = f.getFile().toString();
      if (fileString.equals(testFile)) {
        return f;
      }
    }
    return null;
  }

  public ArrayList<FileData> getFiles() {
    return this.files;
  }

  public boolean hasFile(final File file) {
    for (final FileData f : this.files) {
      final String fileString = file.toString();
      final String testFile = f.getFile().toString();
      if (fileString.equals(testFile)) {
        return true;
      }
    }
    return false;
  }

  @SuppressWarnings("unchecked")
    public void write(final File versionDir) throws IOException {
    final File file = new File(versionDir, this.version + ".json");

    final JSONObject obj = new JSONObject();
    final JSONArray fileArray = new JSONArray();

    int index = 0;
    for (final FileData fData : this.getFiles()) {
      final JSONObject fileData = new JSONObject();
      fileData.setString("file", fData.getFile().toString());
      fileData.setString("action", fData.getAction());
      fileData.setLong("lastModified", fData.getLastModified());
      // TODO zzLater add md5 of file and maybe other means to tell if the file has changed.
      fileArray.setJSONObject(index++, fileData);
    }
    obj.setJSONArray("files", fileArray);

    this.app.saveJSONObject(obj, file.getAbsolutePath());
  }

  @Override
    public String toString() {
    final StringBuilder sb = new StringBuilder();

    sb.append(this.version.toString());
    sb.append(".json");

    return sb.toString();
  }
}
