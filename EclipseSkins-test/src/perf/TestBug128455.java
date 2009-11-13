package perf;

import java.lang.reflect.Method;

import junit.framework.TestCase;

import org.eclipse.core.runtime.Platform;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.LayoutTree;
import org.eclipse.ui.internal.LayoutTreeNode;
import org.eclipse.ui.internal.ViewSashContainer;
import org.eclipse.ui.internal.ViewSite;
import org.eclipse.ui.internal.ViewStack;
import org.eclipse.ui.presentations.IPresentablePart;
import org.eclipse.ui.presentations.IStackPresentationSite;
import org.eclipse.ui.presentations.StackPresentation;

public class TestBug128455 extends TestCase {

    final static String CONSOLE_ID = "org.eclipse.ui.console.ConsoleView";

    Display display = PlatformUI.getWorkbench().getDisplay();

    IViewPart console;

    private IPresentablePart consolePresPart;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        Point size = window.getShell().getSize();
        /*
         * wide enough to be able to resize views, hight enough to paint tabs + toolbars
         */
        size.x = 1050;
        size.y = 220;
        window.getShell().setSize(size);
        IWorkbenchPage activePage = window.getActivePage();

        activePage.closeAllPerspectives(false, false);
        activePage.setPerspective(PlatformUI.getWorkbench().getPerspectiveRegistry()
                .findPerspectiveWithId("EclipseSkins-test.perspective1"));
        display.update();

        for (String id : MyPerspectiveFactory.ALL) {
            IViewPart part = activePage.showView(id);
            activePage.activate(part);
            display.update();
            if (id.equals(CONSOLE_ID)) {
                console = part;
                /*
                 * get console IPresentablePart
                 */
                Method method2 = StackPresentation.class.getDeclaredMethod("getSite",
                        (Class[]) null);
                method2.setAccessible(true);
                StackPresentation pres = ((ViewStack) ((ViewSite) console.getSite())
                        .getPane().getStack()).getTestPresentation();
                IStackPresentationSite tlSite = (IStackPresentationSite) method2.invoke(
                        pres, (Object[]) null);
                consolePresPart = tlSite.getSelectedPart();
            }
        }

        Platform.getJobManager().cancel(null);
        Platform.getJobManager().suspend();
        display.update();
    }

    @Override
    protected void tearDown() throws Exception {
        Platform.getJobManager().resume();
        super.tearDown();
    }

    public void testBug128455() throws Exception {
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

        /*
         * just enjoy the console toolbar flying over another view content :)
         */
        boolean fail = false;
        shakeViews(activePage, rightNode, leftNode, fail);

        /*
         * proof that console visibility state does not match the toolbar visibility
         */
        fail = true;
        shakeViews(activePage, rightNode, leftNode, fail);
    }

    /**
     * @param fail true to fail on bug 128455, false to see it live
     */
    private void shakeViews(IWorkbenchPage activePage, LayoutTree rightNode,
            LayoutTreeNode leftNode, boolean fail) throws PartInitException {
        /*
         * maxCount * resizeStep should be big enough to allow console's toolbar
         * jump from it's position *under* the tab area *in* the tab area and back.
         * This jump seems to be one of the bug's preconditions.
         */
        int maxCount = 20;
        int resizeStep = 30;

        /*
         * move sash to the left
         */
        for (int i = 0; i < maxCount; i++) {
            Rectangle leftBounds = leftNode.getBounds();
            Rectangle rightBounds = rightNode.getBounds();

            leftBounds.width -= resizeStep;
            rightBounds.x -= resizeStep;
            rightBounds.width += resizeStep;

            leftNode.setBounds(leftBounds);
            rightNode.setBounds(rightBounds);

            display.update();
            if(fail) {
                assertEquals(consolePresPart.getControl().isVisible(), consolePresPart
                        .getToolBar().isVisible());
            }
        }

        /*
         * If this for block is commented out, test succeeded, otherwise it would fail later
         * in the next for loop.
         * Together with "jump" of toolbar it is one of bug preconditions.
         * The console view got visible only in setup() method and here, dirung
         * resize it is always hidden. But after activation here console's toolbar get
         * always visible...
         */
        for (String id : MyPerspectiveFactory.ALL) {
            display.update();
            IViewPart viewPart = activePage.showView(id);
            activePage.activate(viewPart);
            display.update();
        }

        if(fail) {
            assertEquals(consolePresPart.getControl().isVisible(), consolePresPart
                    .getToolBar().isVisible());
        }

        /*
         * move sash to the right
         */
        for (int i = 0; i < maxCount; i++) {
            Rectangle leftBounds = leftNode.getBounds();
            Rectangle rightBounds = rightNode.getBounds();

            leftBounds.width += resizeStep;
            rightBounds.x += resizeStep;
            rightBounds.width -= resizeStep;

            leftNode.setBounds(leftBounds);
            rightNode.setBounds(rightBounds);

            display.update();
            if(fail) {
                assertEquals(consolePresPart.getControl().isVisible(), consolePresPart
                        .getToolBar().isVisible());
            }
        }
    }

}
