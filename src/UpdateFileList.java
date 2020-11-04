import java.util.ArrayList;

public class UpdateFileList extends ArrayList<UpdateData> {
	private static final long serialVersionUID = 2392591370264553450L;

	@Override
	public boolean add(final UpdateData updateData) {
		UpdateData oldData = null;
		for (final UpdateData ud : this) {
			if (ud.getFile().equals(updateData.getFile())) {
				oldData = ud;
				this.remove(ud);
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
			return super.add(newData);
		}

		if (Version.debugDetail) System.out.println("Add to Update List: " + updateData);
		return super.add(updateData);
	}

	public void add(final VersionFile vf) {
		for (final FileData fd : vf.getFiles()) this.add(new UpdateData(vf.version, fd));
	}
}
