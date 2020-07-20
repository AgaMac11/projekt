package sample;

import datamodel.Contact;
import datamodel.ContactData;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.util.Callback;

import java.io.IOException;
import java.util.Optional;

public class Controller {
    @FXML
    private TableView<Contact> tableView;
    @FXML
    private BorderPane borderPane;
    @FXML
    private ContextMenu contextMenu;

    private ContactData data;

    public void initialize() {
        data = new ContactData();
        data.loadContacts();
        tableView.setItems(data.getContacts());

        //context menu
        contextMenu = new ContextMenu();
        MenuItem deleteMenuItem = new MenuItem("delete");
        deleteMenuItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                data.removeContact(tableView.getSelectionModel().getSelectedItem());
            }
        });
        contextMenu.getItems().add(deleteMenuItem);
        //lambda
        tableView.setRowFactory(param -> {
            TableRow<Contact> row = new TableRow<>();
            row.emptyProperty().addListener((observable, wasEmpty, isNowEmpty) -> {
                if(isNowEmpty){
                    row.setContextMenu(null);
                } else {
                    row.setContextMenu(contextMenu);
                }
            });
            return row;
        });
    }

    @FXML
    public void showAddContactDialog(){
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.initOwner(borderPane.getScene().getWindow());
        dialog.setTitle("Add new contact");
        //load new fxml
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(getClass().getResource("contactDialog.fxml"));
        try{
            dialog.getDialogPane().setContent(fxmlLoader.load());
        }catch (IOException e){
            e.printStackTrace();
            return;
        }

        dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);

        Optional<ButtonType> result = dialog.showAndWait();
        if(result.isPresent() && result.get()==ButtonType.OK){
            DialogController dialogController = fxmlLoader.getController();
            Contact newContact = dialogController.processResult();
            data.addContact(newContact);
            data.saveContacts();
        }

    }
    @FXML
    public void showEditContactDialog() {
        Contact selectedContact = tableView.getSelectionModel().getSelectedItem();
        if(selectedContact == null) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("blad");
            alert.getButtonTypes().add(ButtonType.CLOSE);
            alert.setContentText("pick contact you wanted to edit");
            alert.showAndWait();
            return;
        }
        Dialog<ButtonType> editDialog = new Dialog<>();
        editDialog.initOwner(borderPane.getScene().getWindow());
        editDialog.setTitle("Edit contact");
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("contactDialog.fxml"));
        try {
            editDialog.getDialogPane().setContent(loader.load());
        } catch (IOException e){
            e.printStackTrace();
        }
        editDialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);
        editDialog.getDialogPane().getButtonTypes().add(ButtonType.OK);

        DialogController dialogController = loader.getController();
        dialogController.editContact(selectedContact);

        Optional<ButtonType> result = editDialog.showAndWait();
        if(result.isPresent() && result.get()==ButtonType.OK) {
            dialogController.updateContact(selectedContact);
            data.saveContacts();
        }

    }

}
