package perf;

import java.util.ArrayList;
import java.util.Arrays;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

public class MyPerspectiveFactory implements IPerspectiveFactory {

    public static final String [] TOP_LEFT = {
        IPageLayout.ID_BOOKMARKS,
        IPageLayout.ID_OUTLINE,
        IPageLayout.ID_PROBLEM_VIEW
    };

    public static final String [] TOP_RIGHT = {
        IPageLayout.ID_TASK_LIST,
        IPageLayout.ID_RES_NAV
    };

    public static final String [] BOTT_LEFT = {
        IPageLayout.ID_PROGRESS_VIEW,
        "org.eclipse.search.SearchResultView",
        "org.eclipse.ui.console.ConsoleView",
        "org.eclipse.search.ui.views.SearchView",
    };

    public static final String [] BOTT_RIGHT = {
        IPageLayout.ID_PROP_SHEET
    };

    public static final String [] ALL ;
    static {
        ArrayList<String> strings = new ArrayList<String>();
        strings.addAll(Arrays.asList(TOP_LEFT));
        strings.addAll(Arrays.asList(TOP_RIGHT));
        strings.addAll(Arrays.asList(BOTT_LEFT));
        strings.addAll(Arrays.asList(BOTT_RIGHT));
        ALL = strings.toArray(new String [strings.size()]);
    }


    public void createInitialLayout(IPageLayout layout) {
        // Get the editor area.
        String editorArea = layout.getEditorArea();

        // Top left: Resource Navigator view and Bookmarks view placeholder
        IFolderLayout topLeft = layout.createFolder("topLeft", IPageLayout.LEFT, 0.7f,
                editorArea);

        for (String id : TOP_LEFT) {
            topLeft.addView(id);
        }

        IFolderLayout topRight = layout.createFolder("topRight", IPageLayout.LEFT, 0.5f,
                "topLeft");
        for (String id : TOP_RIGHT) {
            topRight.addView(id);
        }

        IFolderLayout bottLeft = layout.createFolder("bottLeft", IPageLayout.BOTTOM,
                0.5f, editorArea);
        for (String id : BOTT_LEFT) {
            bottLeft.addView(id);
        }

        IFolderLayout bottRight = layout.createFolder("bottRight", IPageLayout.RIGHT,
                0.5f, "bottLeft");
        bottRight.addView(IPageLayout.ID_PROP_SHEET);
    }

}
