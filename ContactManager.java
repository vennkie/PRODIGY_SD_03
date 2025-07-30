import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class ContactManager extends JFrame {
    private ArrayList<Contact> contacts;
    private DefaultTableModel tableModel;
    private JTable contactTable;

    private JTextField nameField, phoneField, emailField;
    private JButton addButton, updateButton, deleteButton, clearButton;

    private final String DATA_FILE = "contacts.ser";

    public ContactManager() {
        contacts = loadContacts();

        setTitle("Contact Manager");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel inputPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        gbc.insets = new Insets(5,5,5,5);
        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.EAST;
        inputPanel.add(new JLabel("Name:"), gbc);

        gbc.gridx = 1; gbc.gridy = 0; gbc.anchor = GridBagConstraints.WEST;
        nameField = new JTextField(15);
        inputPanel.add(nameField, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.anchor = GridBagConstraints.EAST;
        inputPanel.add(new JLabel("Phone:"), gbc);

        gbc.gridx = 1; gbc.gridy = 1; gbc.anchor = GridBagConstraints.WEST;
        phoneField = new JTextField(15);
        inputPanel.add(phoneField, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.anchor = GridBagConstraints.EAST;
        inputPanel.add(new JLabel("Email:"), gbc);

        gbc.gridx = 1; gbc.gridy = 2; gbc.anchor = GridBagConstraints.WEST;
        emailField = new JTextField(15);
        inputPanel.add(emailField, gbc);

        JPanel buttonsPanel = new JPanel();
        addButton = new JButton("Add Contact");
        updateButton = new JButton("Update Contact");
        deleteButton = new JButton("Delete Contact");
        clearButton = new JButton("Clear Fields");

        updateButton.setEnabled(false);
        deleteButton.setEnabled(false);

        buttonsPanel.add(addButton);
        buttonsPanel.add(updateButton);
        buttonsPanel.add(deleteButton);
        buttonsPanel.add(clearButton);

        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        inputPanel.add(buttonsPanel, gbc);

        add(inputPanel, BorderLayout.NORTH);

        String[] columns = { "Name", "Phone", "Email" };
        tableModel = new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int row, int column) { return false; }
        };
        contactTable = new JTable(tableModel);
        contactTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(contactTable);
        add(scrollPane, BorderLayout.CENTER);

        refreshTable();

        addButton.addActionListener(e -> addContact());
        updateButton.addActionListener(e -> updateContact());
        deleteButton.addActionListener(e -> deleteContact());
        clearButton.addActionListener(e -> clearFields());

        contactTable.getSelectionModel().addListSelectionListener(event -> {
            boolean selected = contactTable.getSelectedRow() != -1;
            if (selected) {
                int index = contactTable.getSelectedRow();
                Contact c = contacts.get(index);
                nameField.setText(c.name);
                phoneField.setText(c.phone);
                emailField.setText(c.email);
                updateButton.setEnabled(true);
                deleteButton.setEnabled(true);
                addButton.setEnabled(false);
            } else {
                clearFields();
            }
        });

        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void addContact() {
        String name = nameField.getText().trim();
        String phone = phoneField.getText().trim();
        String email = emailField.getText().trim();

        if (!validateInput(name, phone, email)) return;

        contacts.add(new Contact(name, phone, email));
        saveContacts();
        refreshTable();
        clearFields();
        JOptionPane.showMessageDialog(this, "Contact added successfully!");
    }

    private void updateContact() {
        int index = contactTable.getSelectedRow();
        if (index == -1) {
            JOptionPane.showMessageDialog(this, "Please select a contact to update.");
            return;
        }

        String name = nameField.getText().trim();
        String phone = phoneField.getText().trim();
        String email = emailField.getText().trim();

        if (!validateInput(name, phone, email)) return;

        Contact contact = contacts.get(index);
        contact.name = name;
        contact.phone = phone;
        contact.email = email;

        saveContacts();
        refreshTable();
        clearFields();
        JOptionPane.showMessageDialog(this, "Contact updated successfully!");
    }

    private void deleteContact() {
        int index = contactTable.getSelectedRow();
        if (index == -1) {
            JOptionPane.showMessageDialog(this, "Please select a contact to delete.");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete the selected contact?", "Confirm Delete",
                JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            contacts.remove(index);
            saveContacts();
            refreshTable();
            clearFields();
            JOptionPane.showMessageDialog(this, "Contact deleted successfully!");
        }
    }

    private void clearFields() {
        nameField.setText("");
        phoneField.setText("");
        emailField.setText("");
        contactTable.clearSelection();
        updateButton.setEnabled(false);
        deleteButton.setEnabled(false);
        addButton.setEnabled(true);
    }

    private boolean validateInput(String name, String phone, String email) {
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Name cannot be empty.", "Input Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        if (phone.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Phone cannot be empty.", "Input Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        if (!email.isEmpty() && !email.matches("^\\S+@\\S+\\.\\S+$")) {
            JOptionPane.showMessageDialog(this, "Please enter a valid email address or leave it empty.", "Input Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }

    private void refreshTable() {
        tableModel.setRowCount(0);
        for (Contact c : contacts) {
            tableModel.addRow(new Object[] { c.name, c.phone, c.email });
        }
    }

    private void saveContacts() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(DATA_FILE))) {
            oos.writeObject(contacts);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error saving contacts: " + e.getMessage(), "Save Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    @SuppressWarnings("unchecked")
    private ArrayList<Contact> loadContacts() {
        File file = new File(DATA_FILE);
        if (!file.exists()) return new ArrayList<>();

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            return (ArrayList<Contact>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            JOptionPane.showMessageDialog(this, "Error loading contacts: " + e.getMessage(), "Load Error", JOptionPane.ERROR_MESSAGE);
            return new ArrayList<>();
        }
    }

    static class Contact implements Serializable {
        private static final long serialVersionUID = 1L;

        String name, phone, email;

        Contact(String name, String phone, String email) {
            this.name = name;
            this.phone = phone;
            this.email = email;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ContactManager::new);
    }
}
