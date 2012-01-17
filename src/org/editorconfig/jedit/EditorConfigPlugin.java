
package org.editorconfig.jedit;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import org.gjt.sp.jedit.Buffer;
import org.gjt.sp.jedit.EBComponent;
import org.gjt.sp.jedit.EBMessage;
import org.gjt.sp.jedit.EditBus;
import org.gjt.sp.jedit.EditPlugin;
import org.gjt.sp.jedit.buffer.JEditBuffer;
import org.gjt.sp.jedit.msg.BufferUpdate;
import org.gjt.sp.util.Log;

public class EditorConfigPlugin extends EditPlugin implements EBComponent
{
    @Override
    public void start()
    {
        EditBus.addToBus(this);
    }

    @Override
    public void stop()
    {
        EditBus.removeFromBus(this);
    }

    static private void loadEditorConfig(Buffer buf)
    {
        Process proc;
        try
        {
            proc = new ProcessBuilder("editorconfig", buf.getPath()).start();
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return;
        }

        InputStreamReader isr = new InputStreamReader(
                proc.getInputStream());
        BufferedReader br = new BufferedReader(isr);

        String line;
        while (true)
        {
            try
            {
                if ((line = br.readLine()) == null)
                    break;
            }
            catch(IOException e)
            {
                e.printStackTrace();
                break;
            }

            // Get the position of '='
            int eq_pos = line.indexOf('=');

            if (eq_pos == -1 || // = is not found, skip this line
                    eq_pos == 0 || // Left side of = is empty
                    eq_pos == line.length() - 1) // right side is empty
                continue;

            String key = line.substring(0, eq_pos).trim();
            String value = line.substring(eq_pos + 1).trim();

            if (key.equals("indent_style")) // soft or hard tabs?
            {
                if (value.equals("tab"))
                    buf.setBooleanProperty("noTabs", false);
                else if (value.equals("space"))
                    buf.setBooleanProperty("noTabs", true);
            }
            else if (key.equals("tab_width")) // the width of tab
            {
                int tab_width = Integer.parseInt(value);

                if (tab_width > 0)
                    buf.setIntegerProperty("tabSize", tab_width);
            }
            else if (key.equals("indent_size")) // the size of indent
            {
                int indent_size = Integer.parseInt(value);

                if (indent_size > 0)
                    buf.setIntegerProperty("indentSize", indent_size);
            }
            else if (key.equals("end_of_line")) // eof
            {
                if (value.equals("lf"))
                    buf.setStringProperty(JEditBuffer.LINESEP, "\n");
                else if (value.equals("crlf"))
                    buf.setStringProperty(JEditBuffer.LINESEP, "\r\n");
                else if (value.equals("cr"))
                    buf.setStringProperty(JEditBuffer.LINESEP, "\r");
            }
        }
    }
	public void handleMessage(EBMessage msg)
	{
		if (msg instanceof BufferUpdate)
		{
		    BufferUpdate bu_msg = (BufferUpdate) msg;
            Buffer buf = bu_msg.getBuffer();

		    if (bu_msg.getWhat() == BufferUpdate.LOADED)
            {
                loadEditorConfig(buf);
            }
		}
    }
}
