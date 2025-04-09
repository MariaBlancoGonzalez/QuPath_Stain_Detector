/**
 * Script to export image tiles (can be customized in various ways).
 */

// Get the current image (supports 'Run for project')
def imageData = getCurrentImageData()

// Define output path (here, relative to project)
def name = GeneralTools.getNameWithoutExtension(imageData.getServer().getMetadata().getName())
def pathOutput = buildFilePath(PROJECT_BASE_DIR, 'tiles', name)
mkdirs(pathOutput)
println('Output path:')
println(pathOutput)

// Define output resolution in calibrated units (e.g. µm if available)
double requestedPixelSize = 0.25

// Convert output resolution to a downsample factor
double pixelSize = imageData.getServer().getPixelCalibration().getAveragedPixelSize()
double downsample = requestedPixelSize / pixelSize
println('Downsample:')
println(downsample)

def labelServer = new LabeledImageServer.Builder(imageData)
    .backgroundLabel(0, ColorTools.BLACK) // Specify background label (usually 0 or 255)
    .downsample(downsample)    // Choose server resolution; this should match the resolution at which tiles are exported
    .addLabel('Region*', 1)      // Choose output labels (the order matters!)
    .multichannelOutput(false)
    .build()

// Create an exporter that requests corresponding tiles from the original & labelled image servers
new TileExporter(imageData)
    .downsample(downsample)   // Define export resolution
    .imageExtension('.jpg')   // Define file extension for original pixels (often .tif, .jpg, '.png' or '.ome.tif')
    .tileSize(1024)            // Define size of each tile, in pixels
    .labeledImageExtension('.png')
    .annotatedTilesOnly(true) // If true, only export tiles if there is a (classified) annotation present
    .includePartialTiles(true)
    .labeledServer(labelServer) // Define the labeled image server to use (i.e. the one we just built)
    .overlap(0)              // Define overlap, in pixel units at the export resolution
    .writeTiles(pathOutput)   // Write tiles to the specified directory

print 'Done!'