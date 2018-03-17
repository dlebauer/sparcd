package controller.uploadView;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.image.ImageView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import model.constant.SanimalDataFormats;
import model.image.ImageContainer;
import model.image.ImageDirectory;
import org.fxmisc.easybind.EasyBind;

/**
 * Class used as the controller for an upload entry in the treeview
 */
public class UploadTreeCellController extends TreeCell<ImageContainer>
{
	///
	/// FXML Bound Fields start
	///

	// Icon of tree entry
	@FXML
	public ImageView imgIcon;
	// The text to display
	@FXML
	public Label lblText;
	// A reference to the main pane
	@FXML
	public HBox mainPane;

	///
	/// FXML Bound Fields end
	///

	private ChangeListener<Number> expandedListener = (observable, oldValue, newValue) ->
	{
		if (newValue.doubleValue() != -1)
			if (this.getTreeItem() != null)
				this.getTreeItem().setExpanded(false);
	};

	@FXML
	public void initialize()
	{
		this.treeItemProperty().addListener((observable, oldValue, newValue) ->
		{
			if (oldValue != null)
				oldValue.expandedProperty().unbind();
			if (newValue != null)
				newValue.expandedProperty().addListener((ignored, oldExpanded, newExpanded) ->
				{
					if (UploadTreeCellController.this.isDisabled() && newExpanded)
						newValue.setExpanded(false);
				});
		});
	}

	/**
	 * Called when we want to display a new image container
	 *
	 * @param item The new item to display
	 * @param empty If the item is null and the cell should be empty
	 */
	@Override
	protected void updateItem(ImageContainer item, boolean empty)
	{
		// Remove the previous listener if there was one
		if (this.getItem() instanceof ImageDirectory)
		{
			((ImageDirectory) this.getItem()).uploadProgressProperty().removeListener(expandedListener);
			this.disableProperty().unbind();
			this.setDisable(false);
		}

		super.updateItem(item, empty);

		// Set the text to null
		this.setText(null);

		// If the cell is empty we have no graphic
		if (empty && item == null)
		{
			this.setGraphic(null);
		}
		// if the cell is not empty, set the field's values and set the graphic
		else
		{
			this.imgIcon.imageProperty().unbind();
			this.imgIcon.imageProperty().bind(item.getTreeIconProperty());
			this.lblText.setText(item.toString());

			if (item instanceof ImageDirectory)
			{
				ImageDirectory imageDirectory = (ImageDirectory) item;
				this.disableProperty().bind(imageDirectory.uploadProgressProperty().isNotEqualTo(-1));
				imageDirectory.uploadProgressProperty().addListener(expandedListener);
			}

			this.setGraphic(mainPane);
		}
	}

	public void cellDragDetected(MouseEvent mouseEvent)
	{
		// Grab the selected image directory, make sure it's not null
		ImageContainer selected = this.getItem();
		if (selected != null)
		{
			// Can only drag & drop if we have a directory selected
			if (selected instanceof ImageDirectory)
			{
				ImageDirectory selectedDirectory = (ImageDirectory) selected;

				// Make sure we're not uploading
				if (selectedDirectory.getUploadProgress() == -1)
				{
					// Create a dragboard and begin the drag and drop
					Dragboard dragboard = this.startDragAndDrop(TransferMode.ANY);

					// Create a clipboard and put the location unique ID into that clipboard
					ClipboardContent content = new ClipboardContent();
					content.put(SanimalDataFormats.IMAGE_DIRECTORY_FILE_FORMAT, selectedDirectory.getFile());
					// Set the dragboard's context, and then consume the event
					dragboard.setContent(content);

					mouseEvent.consume();
				}
			}
		}
	}
}
