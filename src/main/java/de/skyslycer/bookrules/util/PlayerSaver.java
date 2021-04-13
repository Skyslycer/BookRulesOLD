package de.skyslycer.bookrules.util;

import java.io.*;

public class PlayerSaver {
    public void writeToFile(String value, String file) {
        checkFile(file);
        try {
            FileWriter writer = new FileWriter(file, true);
            writer.write("\n" + value);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean containsInFile(String value, String file) {
        boolean contains = false;
        checkFile(file);
        try {
            FileReader reader = new FileReader(file);
            BufferedReader bufferedReader = new BufferedReader(reader);

            String line;

            while ((line = bufferedReader.readLine()) != null) {
                if(line.equals(value)) {
                    contains = true;
                    break;
                }
            }
            reader.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
        return contains;
    }

    public void replaceInFile(String file, String oldString, String newString) {
        File fileToBeModified = new File(file);
        BufferedReader reader = null;
        FileWriter writer = null;
        try
        {
            reader = new BufferedReader(new FileReader(fileToBeModified));
            String line = reader.readLine();
            while (line != null)
            {
                oldString = oldString + line + System.lineSeparator();
                line = reader.readLine();
            }
            String newContent = oldString.replaceAll(oldString, newString);
            writer = new FileWriter(fileToBeModified);
            writer.write(newContent);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        finally
        {
            try
            {
                reader.close();
                writer.close();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    public void checkFile(String file) {
        try {
            File myObj = new File(file);
            if (myObj.createNewFile()) {}
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
