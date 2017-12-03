package graphing;

import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.stream.file.FileSinkSVG2;
import org.graphstream.ui.view.Viewer;
import org.graphstream.ui.view.ViewerListener;
import org.graphstream.ui.view.ViewerPipe;

import java.io.IOException;
import java.text.MessageFormat;

abstract class GraphVisualizer implements ViewerListener {
    private static final int INITIAL_DELAY = 2000;
    Graph graph;
    ViewerPipe fromViewer;
    boolean updated = false;
    FileSinkSVG2 svgSink;

    private boolean loop = true;

    abstract String getStyleSheetPath();

    abstract String getScreenshotSavePath(String suffix);

    abstract int getSleepDelay();

    abstract void setupNodes();

    abstract boolean updateGraph();

    @Override
    public abstract void buttonPushed(String id);

    @Override
    public abstract void buttonReleased(String id);

    final void visualize() {
        visualize(true);
    }

    final void visualize(boolean saveScreenshot) {
        setupGraph();
        setupNodes();
        long lastTime = System.currentTimeMillis();
//        while (System.currentTimeMillis() - lastTime < INITIAL_DELAY) {};
        int iteration = 0;
        long lastT = System.currentTimeMillis();
        while (loop) {
            fromViewer.pump();
            if (updated) {
                if (System.currentTimeMillis() - lastT >= getSleepDelay()) {
                    iteration++;
                    if (saveScreenshot && iteration == 1) {
                        saveScreenshot("0");
                    }
                    updated = updateGraph();
                    if (updated && saveScreenshot) {
                        saveScreenshot(String.valueOf(iteration));
                    }
                    lastT = System.currentTimeMillis();
                }
            }
        }
    }

    private void setupGraph() {
        System.setProperty("org.graphstream.ui.renderer", "org.graphstream.ui.j2dviewer.J2DGraphRenderer");
        graph = new SingleGraph("graph");
        graph.addAttribute("ui.stylesheet", MessageFormat.format("url({0})", getStyleSheetPath()));
        graph.setAutoCreate(true);
        graph.setStrict(false);
        graph.addAttribute("ui.quality");
        graph.addAttribute("ui.antialias");
        Viewer viewer = graph.display();
        fromViewer = viewer.newViewerPipe();
        fromViewer.addViewerListener(this);
        fromViewer.addSink(graph);
        svgSink = new FileSinkSVG2();
    }

    void saveScreenshot(String suffix) {
        try {
            svgSink.writeAll(graph, MessageFormat.format("{0}.svg", getScreenshotSavePath(suffix)));
        } catch (IOException e) {
            e.printStackTrace();
        }
        graph.addAttribute("ui.screenshot",
                MessageFormat.format("{0}.png", getScreenshotSavePath(suffix)));
    }

    @Override
    public final void viewClosed(String viewName) {
        loop = false;
    }
}