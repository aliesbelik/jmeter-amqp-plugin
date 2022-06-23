package com.zeroclue.jmeter.protocol.amqp.gui;

import org.apache.jmeter.config.Argument;
import org.apache.jmeter.config.gui.ArgumentsPanel;
import org.apache.jorphan.gui.ObjectTableModel;
import org.apache.jorphan.reflect.Functor;

public class AMQPHeadersPanel extends ArgumentsPanel {

    public AMQPHeadersPanel(final String label) {
        super(label);
    }

    @Override
    protected void initializeTableModel() {
        tableModel = new ObjectTableModel(new String[] { COLUMN_RESOURCE_NAMES_0, COLUMN_RESOURCE_NAMES_1, "Type" },
            Argument.class,
            new Functor[] {
                new Functor("getName"), // $NON-NLS-1$
                new Functor("getValue"),  // $NON-NLS-1$
                new Functor("getDescription") },  // $NON-NLS-1$
            new Functor[] {
                new Functor("setName"), // $NON-NLS-1$
                new Functor("setValue"), // $NON-NLS-1$
                new Functor("setDescription") },  // $NON-NLS-1$
            new Class[] { String.class, String.class, String.class });
    }

    @Override
    protected Argument makeNewArgument() {
        return new Argument("", "", null, "string");
    }

}
