package perf;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import junit.framework.TestCase;

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPreferenceConstants;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.LayoutTree;
import org.eclipse.ui.internal.LayoutTreeNode;
import org.eclipse.ui.internal.ViewSashContainer;
import org.eclipse.ui.internal.ViewSite;
import org.eclipse.ui.internal.ViewStack;
import org.eclipse.ui.internal.editors.text.UntitledTextFileWizard;
import org.eclipse.ui.presentations.IPresentablePart;
import org.eclipse.ui.presentations.IStackPresentationSite;
import org.eclipse.ui.presentations.StackPresentation;

public class TestPerformance extends TestCase {
    Display display = PlatformUI.getWorkbench().getDisplay();
    UntitledTextFileWizard wizard;
    int editorsCount = 5;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        IWorkbench workbench = PlatformUI.getWorkbench();
        IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
        Point size = window.getShell().getSize();
        size.y = 320;
        size.x = 1050;
        window.getShell().setSize(size);
        IWorkbenchPage activePage = window.getActivePage();

        IPreferenceStore store = PlatformUI.getPreferenceStore();

        store.putValue(IWorkbenchPreferenceConstants.ENABLE_ANIMATIONS, "false");
        store.putValue(IWorkbenchPreferenceConstants.ENABLE_NEW_MIN_MAX, "false");
		store.putValue(
                IWorkbenchPreferenceConstants.PRESENTATION_FACTORY_ID,
                "de.loskutov.EclipseSkins.extvs.EVSPresentationFactory");

        activePage.closeAllPerspectives(false, false);
        update(display);

        activePage.setPerspective(workbench.getPerspectiveRegistry()
                .findPerspectiveWithId("EclipseSkins-test.perspective1"));
        update(display);

        for (String id : MyPerspectiveFactory.ALL) {
            IViewPart part = activePage.showView(id);
            activePage.bringToTop(part);
            activePage.activate(part);
            update(display);
        }
        for (String id : MyPerspectiveFactory.ALL) {
            IViewPart part = activePage.showView(id);
            activePage.bringToTop(part);
            activePage.activate(part);
            update(display);
        }

        wizard = new UntitledTextFileWizard();
        wizard.init(workbench, null);
        for (int i = 0; i < editorsCount; i++) {
            wizard.performFinish();
        }
        wait(window);
        update(display);
    }

    @Override
    protected void tearDown() throws Exception {
        Platform.getJobManager().resume();
        super.tearDown();
    }

    @SuppressWarnings("boxing")
    public void testMaximizeMinimize() throws Exception {

        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        IWorkbenchPage activePage = window.getActivePage();

        ViewStack tlStack = (ViewStack) ((ViewSite) activePage.showView(
                MyPerspectiveFactory.TOP_LEFT[0]).getSite()).getPane().getStack();

        ViewStack trStack = (ViewStack) ((ViewSite) activePage.showView(
                MyPerspectiveFactory.TOP_RIGHT[0]).getSite()).getPane().getStack();

        ViewStack blStack = (ViewStack) ((ViewSite) activePage.showView(
                MyPerspectiveFactory.BOTT_LEFT[0]).getSite()).getPane().getStack();

        ViewStack brStack = (ViewStack) ((ViewSite) activePage.showView(
                MyPerspectiveFactory.BOTT_RIGHT[0]).getSite()).getPane().getStack();

        StackPresentation tlPres = tlStack.getTestPresentation();
        StackPresentation trPres = trStack.getTestPresentation();
        StackPresentation blPres = blStack.getTestPresentation();
        StackPresentation brPres = brStack.getTestPresentation();

        Method method = StackPresentation.class.getDeclaredMethod("getSite",
                (Class[]) null);
        method.setAccessible(true);
        IStackPresentationSite tlSite = (IStackPresentationSite) method.invoke(tlPres,
                (Object[]) null);
        IStackPresentationSite trSite = (IStackPresentationSite) method.invoke(trPres,
                (Object[]) null);
        IStackPresentationSite blSite = (IStackPresentationSite) method.invoke(blPres,
                (Object[]) null);
        IStackPresentationSite brSite = (IStackPresentationSite) method.invoke(brPres,
                (Object[]) null);


        int maxCount = 2;
        int rounds = 10;
        ArrayList<Long> times = new ArrayList<Long>();
        long start = System.currentTimeMillis();

        for (int n = 0; n < rounds; n++) {
            long start2 = System.currentTimeMillis();

            for (int i = 0; i < maxCount; i++) {

                tlSite.setState(IStackPresentationSite.STATE_MAXIMIZED);
                update(display);
                tlSite.setState(IStackPresentationSite.STATE_MINIMIZED);
                update(display);
                tlSite.setState(IStackPresentationSite.STATE_RESTORED);
                update(display);

                trSite.setState(IStackPresentationSite.STATE_MAXIMIZED);
                update(display);
                trSite.setState(IStackPresentationSite.STATE_MINIMIZED);
                update(display);
                trSite.setState(IStackPresentationSite.STATE_RESTORED);
                update(display);

                tlSite.setState(IStackPresentationSite.STATE_MINIMIZED);
                update(display);
                trSite.setState(IStackPresentationSite.STATE_MINIMIZED);
                update(display);
                tlSite.setState(IStackPresentationSite.STATE_RESTORED);
                update(display);
                trSite.setState(IStackPresentationSite.STATE_RESTORED);
                update(display);

                blSite.setState(IStackPresentationSite.STATE_MAXIMIZED);
                update(display);
                blSite.setState(IStackPresentationSite.STATE_MINIMIZED);
                update(display);
                blSite.setState(IStackPresentationSite.STATE_RESTORED);
                update(display);

                brSite.setState(IStackPresentationSite.STATE_MAXIMIZED);
                update(display);
                brSite.setState(IStackPresentationSite.STATE_MINIMIZED);
                update(display);
                brSite.setState(IStackPresentationSite.STATE_RESTORED);
                update(display);

                blSite.setState(IStackPresentationSite.STATE_MINIMIZED);
                update(display);
                brSite.setState(IStackPresentationSite.STATE_MINIMIZED);
                update(display);
                blSite.setState(IStackPresentationSite.STATE_RESTORED);
                update(display);
                brSite.setState(IStackPresentationSite.STATE_RESTORED);
                update(display);

                blSite.setState(IStackPresentationSite.STATE_MINIMIZED);
                update(display);
                brSite.setState(IStackPresentationSite.STATE_MINIMIZED);
                update(display);
                tlSite.setState(IStackPresentationSite.STATE_MINIMIZED);
                update(display);
                trSite.setState(IStackPresentationSite.STATE_MINIMIZED);
                update(display);
                blSite.setState(IStackPresentationSite.STATE_RESTORED);
                update(display);
                brSite.setState(IStackPresentationSite.STATE_RESTORED);
                update(display);
                tlSite.setState(IStackPresentationSite.STATE_RESTORED);
                update(display);
                trSite.setState(IStackPresentationSite.STATE_RESTORED);
                update(display);
            }

            long stop2 = System.currentTimeMillis();
            times.add(stop2 - start2);
//          n = askContinue(window, rounds, n);
        }

        long stop = System.currentTimeMillis();

        System.out.println("---[ max min ]-------");
        Long median = median(times);
        System.out.println(median + " ms");
        System.out.println((stop - start) + " ms");
        System.out.println("---------------------\n");
        // TODO only for navigation
        assertTrue("MaxMin performance problem", median < 2300); // 2156 <-> 2156
    }

    @SuppressWarnings("boxing")
    public void testOpenClose() throws Exception {

        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        IWorkbenchPage activePage = window.getActivePage();

        ViewStack tlStack = (ViewStack) ((ViewSite) activePage.showView(
                MyPerspectiveFactory.TOP_LEFT[0]).getSite()).getPane().getStack();

        ViewStack trStack = (ViewStack) ((ViewSite) activePage.showView(
                MyPerspectiveFactory.TOP_RIGHT[0]).getSite()).getPane().getStack();

        ViewStack blStack = (ViewStack) ((ViewSite) activePage.showView(
                MyPerspectiveFactory.BOTT_LEFT[0]).getSite()).getPane().getStack();

        ViewStack brStack = (ViewStack) ((ViewSite) activePage.showView(
                MyPerspectiveFactory.BOTT_RIGHT[0]).getSite()).getPane().getStack();

        StackPresentation tlPres = tlStack.getTestPresentation();
        StackPresentation trPres = trStack.getTestPresentation();
        StackPresentation blPres = blStack.getTestPresentation();
        StackPresentation brPres = brStack.getTestPresentation();

        Method method = StackPresentation.class.getDeclaredMethod("getSite",
                (Class[]) null);
        method.setAccessible(true);
        IStackPresentationSite tlSite = (IStackPresentationSite) method.invoke(tlPres,
                (Object[]) null);
        IStackPresentationSite trSite = (IStackPresentationSite) method.invoke(trPres,
                (Object[]) null);
        IStackPresentationSite blSite = (IStackPresentationSite) method.invoke(blPres,
                (Object[]) null);
        IStackPresentationSite brSite = (IStackPresentationSite) method.invoke(brPres,
                (Object[]) null);



        int maxCount = 1;
        int rounds = 10;
        ArrayList<Long> times = new ArrayList<Long>();
        long start = System.currentTimeMillis();

        for (int n = 0; n < rounds; n++) {
            long start2 = System.currentTimeMillis();

            for (int i = 0; i < maxCount; i++) {

                for (String id : MyPerspectiveFactory.TOP_LEFT) {
                    activePage.showView(id);
                    update(display);
                    tlSite.close(new IPresentablePart[] {tlSite.getSelectedPart()});
                    update(display);
                }
                for (String id : MyPerspectiveFactory.TOP_LEFT) {
                    activePage.showView(id);
                    update(display);
                }

                for (String id : MyPerspectiveFactory.TOP_RIGHT) {
                    activePage.showView(id);
                    update(display);
                    trSite.close(new IPresentablePart[] {trSite.getSelectedPart()});
                    update(display);
                }
                for (String id : MyPerspectiveFactory.TOP_RIGHT) {
                    activePage.showView(id);
                    update(display);
                }

                for (String id : MyPerspectiveFactory.BOTT_LEFT) {
                    activePage.showView(id);
                    update(display);
                    blSite.close(new IPresentablePart[] {blSite.getSelectedPart()});
                    update(display);
                }
                for (String id : MyPerspectiveFactory.BOTT_LEFT) {
                    activePage.showView(id);
                    update(display);
                }

                for (String id : MyPerspectiveFactory.BOTT_RIGHT) {
                    activePage.showView(id);
                    update(display);
                    brSite.close(new IPresentablePart[] {brSite.getSelectedPart()});
                    update(display);
                }
                for (String id : MyPerspectiveFactory.BOTT_RIGHT) {
                    activePage.showView(id);
                    update(display);
                }

                for (int k = 0; k < editorsCount; k++) {
                    IEditorPart editor = activePage.getActiveEditor();
                    if(editor != null) {
                        activePage.closeEditor(editor, false);
                        update(display);
                    }
                }
                for (int k = 0; k < editorsCount; k++) {
                    wizard.performFinish();
                    update(display);
                }
            }

            long stop2 = System.currentTimeMillis();
            times.add(stop2 - start2);
//            n = askContinue(window, rounds, n);
        }

        long stop = System.currentTimeMillis();

        System.out.println("---[ open close ]----");
        Long median = median(times);
        System.out.println(median + " ms");
        System.out.println((stop - start) + " ms");
        System.out.println("---------------------\n");
        // TODO only for navigation
        assertTrue("OpneClose performance problem", median < 1550); // 1484 <-> 1438
    }

    int askContinue(IWorkbenchWindow window, int rounds, int n) {
        if (n == rounds - 1) {
            if (MessageDialog
                    .openConfirm(window.getShell(), "Continue?", "Continue?")) {
                n = 0;
            }
        }
        return n;
    }

    @SuppressWarnings("boxing")
    public void testResize() throws Exception {
        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        IWorkbenchPage activePage = window.getActivePage();

        IViewPart part = activePage.showView(MyPerspectiveFactory.TOP_LEFT[0]);

        ViewStack stack1 = (ViewStack) ((ViewSite) part.getSite()).getPane().getStack();

        ViewSashContainer container = (ViewSashContainer) stack1.getContainer();

        LayoutTreeNode layoutTree = (LayoutTreeNode) container.getLayoutTree();

        Method method = LayoutTreeNode.class.getDeclaredMethod("getChild",
                new Class[] { boolean.class });
        method.setAccessible(true);
        LayoutTreeNode layoutTree2 = (LayoutTreeNode) method.invoke(layoutTree,
                new Object[] { Boolean.TRUE });
        LayoutTree rightNode = (LayoutTree) method.invoke(layoutTree2,
                new Object[] { Boolean.FALSE });
        LayoutTreeNode leftNode = (LayoutTreeNode) method.invoke(layoutTree2,
                new Object[] { Boolean.TRUE });

        //        LayoutPart sash = layoutTree.getSash();

        int maxCount = 30;
        int resizeStep = 20;
        int rounds = 4;
        ArrayList<Long> times = new ArrayList<Long>();
        long start = System.currentTimeMillis();

        for (int n = 0; n < rounds; n++) {
            long start2 = System.currentTimeMillis();

            for (int i = 0; i < maxCount; i++) {
                Rectangle leftBounds = leftNode.getBounds();
                Rectangle rightBounds = rightNode.getBounds();

                leftBounds.width -= resizeStep;
                rightBounds.x -= resizeStep;
                rightBounds.width += resizeStep;

                leftNode.setBounds(leftBounds);
                rightNode.setBounds(rightBounds);

                update(display);
            }

            for (String id : MyPerspectiveFactory.ALL) {
                activePage.showView(id);
                update(display);
            }

            for (int i = 0; i < maxCount; i++) {
                Rectangle leftBounds = leftNode.getBounds();
                Rectangle rightBounds = rightNode.getBounds();

                leftBounds.width += resizeStep;
                rightBounds.x += resizeStep;
                rightBounds.width -= resizeStep;

                leftNode.setBounds(leftBounds);
                rightNode.setBounds(rightBounds);

                update(display);
            }
            long stop2 = System.currentTimeMillis();
            times.add(stop2 - start2);
//          n = askContinue(window, rounds, n);
        }

        long stop = System.currentTimeMillis();

        System.out.println("---[ resize ]--------");
        Long median = median(times);
        System.out.println(median + " ms");
        System.out.println((stop - start) + " ms");
        System.out.println("---------------------\n");
        // TODO only for navigation
        assertTrue("Resize performance problem", median < 4900); // 4687 <-> 4805
    }

    private static void update(Display display) {
        display.update();
//        display.readAndDispatch();
    }

    private void wait(IWorkbenchWindow window) {
        synchronized (this) {
            try {
                Job.getJobManager().cancel(null);
                wait(300);
                Job.getJobManager().suspend();
                wait(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
//        MessageDialog.openInformation(window.getShell(), "Wait...", "Waiting...");
    }

    /**
     * The median is the middle of a distribution: half the scores are
     * above the median and half are below the median.
     * When there is an odd number of numbers, the median is
     * simply the middle number. For example, the median of 2, 4, and 7 is 4.
     * When there is an even number of numbers, the median is the mean
     * of the two middle numbers. Thus, the median of the numbers
     * 2, 4, 7, 12 is (4+7)/2 = 5.5.
     * @param values will be sorted automatically
     * @return the median of given array, or NaN if array was empty or null
     */
    @SuppressWarnings("boxing")
    public static Long median(List<Long> values) {
        if (values == null || values.size() == 0) {
            return null;
        }
        Collections.sort(values);
        int size = values.size();
        if (size % 2 == 1) {
            return values.get(size / 2);
        }
        return (values.get(size / 2) + values.get(size / 2 - 1)) / 2;
    }

}
