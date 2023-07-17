import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.TimeZone;

public class timeConversionByCountry extends JFrame {
    private JLabel localTimeLabel;
    private String jdbcURL = "jdbc:mysql://localhost:3306/timeConversion";
    private String username = "root";
    private String password = "password";

    public timeConversionByCountry() {
        setTitle("Time Conversion by Country");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        createUI();
        pack(); // Adjust the frame size based on the components' preferred size
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void createUI() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        JPanel inputPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        JPanel buttonPanel = new JPanel();

        JLabel countryLabel = new JLabel("Country:");
        JTextField countryField = new JTextField();

        JLabel timezoneLabel = new JLabel("Timezone:");
        JComboBox<String> timezoneComboBox = new JComboBox<>(ZoneId.getAvailableZoneIds().toArray(new String[0]));

        JButton convertButton = new JButton("Convert");
        localTimeLabel = new JLabel();
        localTimeLabel.setHorizontalAlignment(SwingConstants.CENTER);

        convertButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String country = countryField.getText();
                String timezone = (String) timezoneComboBox.getSelectedItem();
                convertTime(country, timezone);

                // Store the time conversion data in the database
                try (Connection connection = DriverManager.getConnection(jdbcURL, username, password)) {
                    String sql = "INSERT INTO time_conversion (country, timezone, converted_time) VALUES (?, ?, NOW())";
                    PreparedStatement statement = connection.prepareStatement(sql);
                    statement.setString(1, country);
                    statement.setString(2, timezone);
                    statement.executeUpdate();
                    statement.close();
                    System.out.println("Data saved to the database.");
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(timeConversionByCountry.this,
                            "Error saving data to the database.",
                            "Database Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        inputPanel.add(countryLabel);
        inputPanel.add(countryField);
        inputPanel.add(timezoneLabel);
        inputPanel.add(timezoneComboBox);

        buttonPanel.add(convertButton);

        mainPanel.add(inputPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        mainPanel.add(localTimeLabel, BorderLayout.NORTH);

        add(mainPanel);
    }

    private void convertTime(String country, String timezone) {
        Calendar localTime = Calendar.getInstance(TimeZone.getTimeZone(timezone));
        int hour = localTime.get(Calendar.HOUR);
        int minute = localTime.get(Calendar.MINUTE);
        int second = localTime.get(Calendar.SECOND);
        int day = localTime.get(Calendar.DAY_OF_MONTH);
        int month = localTime.get(Calendar.MONTH) + 1; //+1 as it's zero-based
        int year = localTime.get(Calendar.YEAR);
        int amPm = localTime.get(Calendar.AM_PM);
        String amPmStr = (amPm == Calendar.AM) ? "AM" : "PM";

        localTimeLabel.setText("Local time in " + country + " is: " + hour + ":" + minute + ":" + second + " " + amPmStr + " " + day + "-" + month + "-" + year);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new timeConversionByCountry());
    }
}