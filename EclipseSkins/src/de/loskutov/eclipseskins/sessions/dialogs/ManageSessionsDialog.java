/*******************************************************************************
 * Copyright (c) 2006 Andrei Loskutov.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the BSD License
 * which accompanies this distribution, and is available at
 * http://www.opensource.org/licenses/bsd-license.php
 * Contributor:  Andrei Loskutov - initial API and implementation
 *******************************************************************************/
package de.loskutov.eclipseskins.sessions.dialogs;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import de.loskutov.eclipseskins.PresentationPlugin;
import de.loskutov.eclipseskins.ThemeConstants;
import de.loskutov.eclipseskins.sessions.EditingSession;
import de.loskutov.eclipseskins.sessions.OpenSessionAction;
import de.loskutov.eclipseskins.sessions.SessionNameValidator;
import de.loskutov.eclipseskins.sessions.Sessions;

/**
 * @author Andrei
 *
 */
public class ManageSessionsDialog extends MessageDialog {

    final static int COL_NAME = 0;

    final static int COL_EDITORS = 1;

    final static int COL_DATE = 2;

    final static int COL_PATH = 3;


    private static final class TableLabelProvider implements ITableLabelProvider {
        final DateFormat formatter = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
        public void addListener(ILabelProviderListener listener) {
            //
        }

        public void dispose() {
            //
        }

        public boolean isLabelProperty(Object element, String property) {
            return true;
        }

        public void removeListener(ILabelProviderListener listener) {
            //
        }

        public Image getColumnImage(Object element, int columnIndex) {
            return null;
        }

        public String getColumnText(Object element, int columnIndex) {
            if (!(element instanceof EditingSession)) {
                return null;
            }
            EditingSession session = (EditingSession) element;
            switch (columnIndex) {
            case COL_NAME:
                return session.getName();
            case COL_EDITORS:
                return "" + session.getEditors().size();
            case COL_DATE:
                return formatter.format(new Date(session.getPath().lastModified()));
            case COL_PATH:
                return session.getPath().toString();

            default:
                return null;
            }
        }
    }

    private static final class ColumnComparator extends ViewerComparator {

        private final int column;

        private boolean switchDirection;

        public ColumnComparator(int column) {
            this.column = column;
        }

        public int compare(Viewer viewer, Object e1, Object e2) {
            EditingSession s1 = (EditingSession) e1;
            EditingSession s2 = (EditingSession) e2;
            int result = 0;
            switch (column) {
            case COL_NAME:
                result = s1.getName().compareTo(s2.getName());
                break;
            case COL_EDITORS:
                result = new Integer(s1.getEditors().size()).compareTo(new Integer(s2
                        .getEditors().size()));
                break;
            case COL_DATE:
                result = new Date(s1.getPath().lastModified()).compareTo(new Date(s2
                        .getPath().lastModified()));
                break;
            case COL_PATH:
                result = s1.getPath().toString().compareTo(s2.getPath().toString());
                break;
            default:
                break;
            }
            if (switchDirection) {
                result = -result;
            }
            return result;
        }

        public void switchDirection() {
            switchDirection = !switchDirection;
        }
    }

    private Button closeEditorsCheckbox;

    private Button askBeforeCloseCheckbox;

    private Button overrideCheckbox;

    private Label tableLabel;

    private Table sessionsTable;

    private TableViewer tableViewer;

    //    private Button exportButton;
    //
    //    private Button importButton;

    private Button duplicateButton;

    private Button openButton;

    private Button renameButton;

    private Button deleteButton;

    /**
     * @param parentShell
     * @param dialogTitle
     * @param image
     * @param message
     * @param dialogImageType
     * @param dialogButtonLabels
     * @param defaultIndex
     */
    protected ManageSessionsDialog(Shell parentShell, String dialogTitle, Image image,
            String message, int dialogImageType, String[] dialogButtonLabels,
            int defaultIndex) {
        super(parentShell, dialogTitle, image, message, dialogImageType,
                dialogButtonLabels, defaultIndex);
        setShellStyle(SWT.RESIZE | getShellStyle());
    }

    public static ManageSessionsDialog create(Shell parentShell) {
        String dialogTitle = "Manage sessions";
        int dialogImageType = MessageDialog.NONE;
        String[] dialogButtonLabels = new String[] { "OK" };
        int defaultIndex = 0;
        ManageSessionsDialog dialog = new ManageSessionsDialog(parentShell, dialogTitle,
                null, null, dialogImageType, dialogButtonLabels, defaultIndex);

        return dialog;
    }

    protected IDialogSettings getDialogBoundsSettings() {
        IDialogSettings mainSettings = PresentationPlugin.getDefault()
        .getDialogSettings();
        IDialogSettings mySettings = mainSettings.getSection("manageSessionsDialog");
        if (mySettings == null) {
            IDialogSettings newSection = mainSettings
            .addNewSection("manageSessionsDialog");
            return newSection;
        }
        return mySettings;
    }

    protected Point getInitialSize() {
        Point p = super.getInitialSize();
        if (p.x < 400) {
            p.x = 400;
        }
        if (p.y < 300) {
            p.y = 300;
        }
        return p;
    }

    protected Control createCustomArea(Composite parent) {

        // top level container
        Composite container = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.numColumns = 2;
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        container.setLayout(layout);
        GridData gd = new GridData(GridData.FILL_BOTH);
        container.setLayoutData(gd);

        closeEditorsCheckbox = new Button(container, SWT.CHECK);
        closeEditorsCheckbox.setText("Close opened editors before restoring session");
        gd = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
        gd.horizontalSpan = 2;
        closeEditorsCheckbox.setLayoutData(gd);

        final IPreferenceStore prefs = PresentationPlugin.getDefault()
        .getPreferenceStore();
        closeEditorsCheckbox.addSelectionListener(new SelectionListener() {
            public void widgetSelected(SelectionEvent se) {
                boolean selection = closeEditorsCheckbox.getSelection();
                askBeforeCloseCheckbox.setEnabled(selection);
                prefs.setValue(ThemeConstants.CLOSE_EDITORS, selection);
            }

            public void widgetDefaultSelected(SelectionEvent se) {
                /** ignored */
            }
        });

        closeEditorsCheckbox.setSelection(prefs.getBoolean(ThemeConstants.CLOSE_EDITORS));

        askBeforeCloseCheckbox = new Button(container, SWT.CHECK);
        askBeforeCloseCheckbox.setText("Warn before closing editors");
        gd = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
        gd.horizontalSpan = 2;
        askBeforeCloseCheckbox.setLayoutData(gd);

        askBeforeCloseCheckbox.setEnabled(closeEditorsCheckbox.getSelection());
        askBeforeCloseCheckbox.setSelection(prefs
                .getBoolean(ThemeConstants.ASK_BEFORE_CLOSE));
        askBeforeCloseCheckbox.addSelectionListener(new SelectionListener() {
            public void widgetSelected(SelectionEvent se) {
                boolean selection = askBeforeCloseCheckbox.getSelection();
                prefs.setValue(ThemeConstants.ASK_BEFORE_CLOSE, selection);
            }

            public void widgetDefaultSelected(SelectionEvent se) {
                /** ignored */
            }
        });

        overrideCheckbox = new Button(container, SWT.CHECK);
        overrideCheckbox.setText("Override existing session without warning");
        gd = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
        gd.horizontalSpan = 2;
        overrideCheckbox.setLayoutData(gd);

        overrideCheckbox.setSelection(prefs
                .getBoolean(ThemeConstants.OVERRIDE_EXISTING_SESSION));
        overrideCheckbox.addSelectionListener(new SelectionListener() {
            public void widgetSelected(SelectionEvent se) {
                boolean selection = overrideCheckbox.getSelection();
                prefs.setValue(ThemeConstants.OVERRIDE_EXISTING_SESSION, selection);
            }

            public void widgetDefaultSelected(SelectionEvent se) {
                /** ignored */
            }
        });

        //table label
        tableLabel = new Label(container, SWT.NONE);
        tableLabel.setText("Existing sessions");
        gd = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
        gd.horizontalSpan = 2;
        tableLabel.setLayoutData(gd);

        sessionsTable = new Table(container, SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI
                | SWT.SINGLE | SWT.FULL_SELECTION | SWT.BORDER);
        sessionsTable.setHeaderVisible(true);
        sessionsTable.setLayoutData(new GridData(GridData.FILL_BOTH));

        TableLayout tlayout = new TableLayout();
        tlayout.addColumnData(new ColumnWeightData(20, 50));
        tlayout.addColumnData(new ColumnWeightData(10, 50));
        tlayout.addColumnData(new ColumnWeightData(30, 50));
        //        tlayout.addColumnData(new ColumnWeightData(40, 150));
        sessionsTable.setLayout(tlayout);

        Listener sortListener = new Listener() {
            public void handleEvent(Event e) {
                TableColumn column = (TableColumn) e.widget;
                TableColumn oldColumn = sessionsTable.getSortColumn();
                sessionsTable.setSortColumn(column);
                ColumnComparator comp = (ColumnComparator) column.getData("comparator");
                if (oldColumn == column) {
                    sessionsTable
                    .setSortDirection(sessionsTable.getSortDirection() == SWT.UP ? SWT.DOWN
                            : SWT.UP);
                    comp.switchDirection();
                    tableViewer.refresh();
                } else {
                    tableViewer.setComparator(comp);
                }
            }
        };

        ColumnComparator dateComparator = new ColumnComparator(COL_DATE);

        TableColumn tableCol = new TableColumn(sessionsTable, SWT.LEFT);
        tableCol.setResizable(true);
        tableCol.setText("Name");
        tableCol.addListener(SWT.Selection, sortListener);
        tableCol.setData("comparator", new ColumnComparator(COL_NAME));

        tableCol = new TableColumn(sessionsTable, SWT.LEFT);
        tableCol.setResizable(true);
        tableCol.setText("Editors");
        tableCol.addListener(SWT.Selection, sortListener);
        tableCol.setData("comparator", new ColumnComparator(COL_EDITORS));

        tableCol = new TableColumn(sessionsTable, SWT.LEFT);
        tableCol.setResizable(true);
        tableCol.setText("Last modified");
        tableCol.addListener(SWT.Selection, sortListener);
        tableCol.setData("comparator", dateComparator);

        sessionsTable.setSortColumn(tableCol);
        sessionsTable.setSortDirection(SWT.DOWN);

        //        tableCol = new TableColumn(sessionsTable, SWT.LEFT);
        //        tableCol.setResizable(true);
        //        tableCol.setText("Path");
        //        tableCol.addListener(SWT.Selection, sortListener);
        //        tableCol.setData("comparator", new ColumnComparator(COL_PATH));

        tableViewer = new TableViewer(sessionsTable);
        tableViewer.setLabelProvider(new TableLabelProvider());

        IContentProvider contentProvider = new IStructuredContentProvider() {
            public void dispose() {
                //
            }

            public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
                //
            }

            public Object[] getElements(Object inputElement) {
                return Sessions.getInstance().getSessions(true).toArray();
            }
        };
        tableViewer.setContentProvider(contentProvider);
        tableViewer.setInput(this);

        dateComparator.switchDirection();
        tableViewer.setComparator(dateComparator);

        tableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
            public void selectionChanged(SelectionChangedEvent event) {
                ISelection selection = event.getSelection();
                boolean empty = selection.isEmpty();
                openButton.setEnabled(!empty);
                renameButton.setEnabled(!empty);
                deleteButton.setEnabled(!empty);
                duplicateButton.setEnabled(!empty);
                //                exportButton.setEnabled(!empty);
            }
        });

        createButtons(container);
        return container;
    }

    private void createButtons(Composite container) {
        // button container
        Composite buttonContainer = new Composite(container, SWT.NONE);
        GridData gd = new GridData(GridData.FILL_VERTICAL);
        buttonContainer.setLayoutData(gd);
        GridLayout buttonLayout = new GridLayout();
        buttonLayout.numColumns = 1;
        buttonLayout.marginHeight = 0;
        buttonLayout.marginWidth = 0;
        buttonContainer.setLayout(buttonLayout);

        // Add filter button
        openButton = new Button(buttonContainer, SWT.PUSH);
        openButton.setText("Load");
        openButton.setToolTipText("");
        gd = new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_BEGINNING);
        openButton.setLayoutData(gd);
        openButton.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event e) {
                EditingSession session = getSelectedSession();
                if (session == null) {
                    return;
                }
                OpenSessionAction osa = new OpenSessionAction(session);
                close();
                osa.run();
            }
        });
        openButton.setEnabled(false);

        //        // Add package button
        //        importButton = new Button(buttonContainer, SWT.PUSH);
        //        importButton.setText("Import");
        //        importButton.setToolTipText("");
        //        gd = getButtonGridData(importButton);
        //        importButton.setLayoutData(gd);
        //        importButton.addListener(SWT.Selection, new Listener() {
        //            public void handleEvent(Event e) {
        //            }
        //        });
        //
        //        // Remove button
        //        exportButton = new Button(buttonContainer, SWT.PUSH);
        //        exportButton.setText("Export");
        //        exportButton.setToolTipText("");
        //        gd = getButtonGridData(exportButton);
        //        exportButton.setLayoutData(gd);
        //        exportButton.addListener(SWT.Selection, new Listener() {
        //            public void handleEvent(Event e) {
        //            }
        //        });
        //        exportButton.setEnabled(false);

        renameButton = new Button(buttonContainer, SWT.PUSH);
        renameButton.setText("Rename");
        renameButton.setToolTipText("");
        gd = getButtonGridData(renameButton);
        renameButton.setLayoutData(gd);
        renameButton.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event e) {
                final EditingSession session = getSelectedSession();
                if (session == null) {
                    return;
                }
                InputDialog dialog = new InputDialog(getShell(), "Rename session",
                        "Enter new session name", session.getName(),
                        new SessionNameValidator(true));
                int result = dialog.open();
                if (result == Window.CANCEL) {
                    return;
                }
                Sessions.getInstance()
                .renameSession(session.getName(), dialog.getValue());
                tableViewer.refresh();
            }
        });
        renameButton.setEnabled(false);

        duplicateButton = new Button(buttonContainer, SWT.PUSH);
        duplicateButton.setText("Duplicate");
        duplicateButton.setToolTipText("");
        gd = getButtonGridData(duplicateButton);
        duplicateButton.setLayoutData(gd);
        duplicateButton.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event e) {
                final EditingSession session = getSelectedSession();
                if (session == null) {
                    return;
                }
                InputDialog dialog = new InputDialog(getShell(), "Duplicate session",
                        "Enter new session name", session.getName(),
                        new SessionNameValidator(false));
                int result = dialog.open();
                if (result == Window.CANCEL) {
                    return;
                }
                Sessions.getInstance().copySession(session.getName(), dialog.getValue());
                tableViewer.refresh();
            }
        });
        duplicateButton.setEnabled(false);

        deleteButton = new Button(buttonContainer, SWT.PUSH);
        deleteButton.setText("Delete");
        deleteButton.setToolTipText("");
        gd = getButtonGridData(deleteButton);
        deleteButton.setLayoutData(gd);
        deleteButton.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event e) {
                final EditingSession session = getSelectedSession();
                if (session == null) {
                    return;
                }
                Sessions.getInstance().deleteSession(session.getName());
                tableViewer.refresh();
            }
        });
        deleteButton.setEnabled(false);
    }

    private GridData getButtonGridData(Button button) {
        GridData gd = new GridData(GridData.FILL_HORIZONTAL
                | GridData.VERTICAL_ALIGN_BEGINNING);
        GC gc = new GC(button);
        gc.setFont(button.getFont());
        FontMetrics fontMetrics = gc.getFontMetrics();
        gc.dispose();
        int widthHint = Dialog.convertHorizontalDLUsToPixels(fontMetrics,
                IDialogConstants.BUTTON_WIDTH);
        gd.widthHint = Math.max(widthHint, button.computeSize(SWT.DEFAULT, SWT.DEFAULT,
                true).x);

        gd.heightHint = Dialog
        .convertVerticalDLUsToPixels(fontMetrics, 14 /*IDialogConstants.BUTTON_HEIGHT*/);
        return gd;
    }

    private EditingSession getSelectedSession() {
        TableItem[] selection = sessionsTable.getSelection();
        if (selection.length != 1) {
            return null;
        }
        EditingSession session = (EditingSession) selection[0].getData();
        return session;
    }

}
