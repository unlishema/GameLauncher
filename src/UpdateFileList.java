import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class UpdateFileList {
  private static final long serialVersionUID = 2392591370264553450L;

  private final List<UpdateData> list = Collections.synchronizedList(new ArrayList<UpdateData>());

  private boolean add(final UpdateData updateData) {
    UpdateData oldData = null;
    for (int i = 0; i < this.size(); i++) {
      final UpdateData ud = this.list.get(i);
      if (ud.getFile().equals(updateData.getFile())) {
        oldData = ud;
        this.list.remove(i);
      }
    }

    if (oldData != null) {
      final String newAction;
      final Version newVersion;
      if (oldData.getAction().equals(FileData.CREATE)) {
        if (!updateData.getAction().equals(FileData.DELETE)) {
          newAction = FileData.CREATE;

          if (updateData.getAction().equals(FileData.MODIFY))
            newVersion = updateData.getVersion();
          else
            newVersion = oldData.getVersion();
        } else {
          newAction = FileData.DELETE;
          newVersion = updateData.getVersion();
        }
      } else if (oldData.getAction().equals(FileData.STABLE)) {
        if (updateData.getAction().equals(FileData.CREATE))
          newAction = FileData.MODIFY;
        else if (updateData.getAction().equals(FileData.MODIFY))
          newAction = FileData.MODIFY;
        else if (updateData.getAction().equals(FileData.DELETE))
          newAction = FileData.DELETE;
        else
          newAction = FileData.STABLE;

        if (!newAction.equals(FileData.STABLE))
          newVersion = updateData.getVersion();
        else
          newVersion = oldData.getVersion();
      } else if (oldData.getAction().equals(FileData.MODIFY)) {
        if (updateData.getAction().equals(FileData.DELETE))
          newAction = FileData.DELETE;
        else
          newAction = FileData.MODIFY;
        newVersion = updateData.getVersion();
      } else if (oldData.getAction().equals(FileData.DELETE)) {
        if (!updateData.getAction().equals(FileData.DELETE))
          newAction = updateData.getAction();
        else
          newAction = FileData.DELETE;
        newVersion = updateData.getVersion();
      } else {
        newAction = FileData.STABLE;
        newVersion = updateData.getVersion();
      }
      final UpdateData newData = new UpdateData(newVersion, newAction, updateData.getFile(), 
        updateData.getLastModified());

      if (Version.debugDetail) System.out.println("Update in Update List: " + newData);
      return this.list.add(newData);
    }

    if (Version.debugDetail) System.out.println("Add to Update List: " + updateData);
    return this.list.add(updateData);
  }

  public void add(final VersionFile vf) {
    for (final FileData fd : vf.getFiles()) this.add(new UpdateData(vf.version, fd));
  }

  public List<UpdateData> get() {
    return this.list;
  }

  public int size() {
    return this.list.size();
  }
}
