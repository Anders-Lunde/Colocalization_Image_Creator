# Colocalization Image Creator
## An ImageJ plugin for cell-by-cell semi-automatic object based colocalization analysis (OBCA): Customizable image processing of multichannel 3D images for enhanced visualization of features relevant to OBCA
![alt text](https://github.com/Anders-Lunde/Colocalization_Image_Creator/blob/master/Plugin%20overview.png "Plugin overview")

## NB! This is the repository for Plugin 1 in the above image. Plugin 2 has its [own repository here](https://github.com/Anders-Lunde/Colocalization_Object_Counter).
## NB 2! Both plugins 1 + 2, including the Excel and Matlab files are automatically installed by adding our update site (see Installation - option 1).

In this repository you can find:
1. the plugin .jar file
2. the plugin source code
3. Example data, as used in the published article.


## Installing the plugin:
### Option 1 - Recommended!: 
1. Requires FIJI installation: https://imagej.net/Fiji/Downloads.
2. Open FIJI and click Help>Update>Manage Update Sites>Add update site
3. Add https://sites.imagej.net/ObjectColocalizationPlugins/ as the URL. 
4. Click Close>Apply changes.

### Option 2):
Download the most recent .jar files from urls below, and put in your ImageJ/FIJI /plugins/ folder:

[Link to plugin 1 JAR file](https://github.com/Anders-Lunde/Colocalization_Image_Creator/tree/master/jar-file) (Click on the .jar file, and click "download" top right).

[Link to plugin 2 JAR file](https://github.com/Anders-Lunde/Colocalization_Object_Counter/tree/master/jar-file) (Click on the .jar file, and click "download" top right).


## Full description of the plugin can be found in the original publication:
Currently in review in [Scientific Reports] (https://www.nature.com/srep/) (Lunde&Glover 2020).



### The Colocalization Image Creator plugin allows the creation of custom binary or grayscale visualizations based on raw data of input multichannel images, into a format that is more suitable for semi-automatic OBCA. The output visualizations can be based on a single input channel, or represent signal overlap across input channels, thus providing the opportunity to assess which markers any labeled cell or cell compartment contains. The visualizations and the markers they represent are assigned in the form of image elements, which are created during plugin operation. An important feature is that the flexibility of the plugin facilitates visualization of cellular marker colocalization in various ways, accommodating different experimental frameworks.

![alt text](https://github.com/Anders-Lunde/Colocalization_Image_Creator/blob/master/Example.jpg "Colocalization Image Creator example")

## Introduction:

Tools and methods that employ fully automatic colocalization analysis have the benefit of providing reproducibility and speed, albeit sometimes at the cost of accuracy. A major reason for lowered accuracy is that full automation of segmentation and object identification is a notoriously difficult problem to solve, especially for complex objects such as neurons 15,16. To address the need for a high-throughput OBCA workflow that does not rely exclusively on fallible automated algorithms and that leverages human visual processing capacity, we have developed a set of tools for semi-automatic OBCA that combines automation for speed with visual/manual verification for accuracy. The tools we present use image binarization and other operations to extract and visualize meaningful colocalization signals, but ultimate quantification is based on a centroid-like approach in which objects are defined by a single point. We emphasize the utility of visual verification and correction of automatic centroid placement, without the need to perform time consuming corrections to object delineations for quantification.

A schematic of the tool-chain workflow is shown in Figure 1. The entry point to the workflow consists of a plugin (the Colocalization Image Creator) for the popular free and open source image analysis software ImageJ 17. The Colocalization Image Creator enables flexible processing of image data into a visual format that is better suited to high-throughput semi-automatic OBCA. It can produce processed binary and grayscale signal outputs, visualize signal overlap across channels, and can produce a special Z-projection where 3D information is condensed onto a 2D plane for easy visualization of 3D colocalization data, in a way that minimizes Z-projection artifacts (examples of such artifacts are shown in Figure 2A). Additionally, signal overlap processing enables restricting visualization to labeled cellular sub-compartments, for example cell nuclei, which can improve object segmentation. This can also minimize artifacts that arise from partially transected objects (example in Figure 2B). 
A second ImageJ plugin (the Colocalization Object Counter) enables OBCA quantification in any type of image. It uses local maxima algorithms to automatically define objects as single points, which can be edited and verified visually in a semi-automatic manner. The Colocalization Object Counter enables annotating which label(s) are associated with each object (object colocalization category). The plugin also contains basic tools for subsequent 3D reconstruction of object and tissue contour data.

A third tool, in the form of a Microsoft Excel macro file, enables data import overview, revision, statistical analysis, and export. Exported data can be imported by a fourth tool, a Matlab script that enables interactive 3D visualization of identified objects with their colocalization categories indicated within visible tissue contours.

Although we have designed and optimized this tool set for semi-automatic OBCA of multi-fluorescence imaging data from the developing central nervous system, the workflow is adaptable to other types of imaging data as well. The tools complement each other, but can also be used individually. Below, we describe the function of each tool, provide some examples of usage, and provide an assessment of robustness across individual operators. We compare our platform head-to-head with similar platforms, and we evaluate specific limitations associated with our platform. In the Supplementary Information we provide specific details on the operation and user interfaces of the tool set.

## Purpose, input requirements, operation and output:

The Colocalization Image Creator plugin allows the creation of custom binary or grayscale visualizations based on raw data of input multichannel images, into a format that is more suitable for semi-automatic OBCA. The output visualizations can be based on a single input channel, or represent signal overlap across input channels, thus providing the opportunity to assess which markers any labeled cell or cell compartment contains. The visualizations and the markers they represent are assigned in the form of image elements (see below), which are created during plugin operation. An important feature is that the flexibility of the plugin facilitates visualization of cellular marker colocalization in various ways, accommodating different experimental frameworks.

The input to the plugin must be a multichannel image in a format that is readable by ImageJ. Both 3D (Z-stacks) and 2D (single plane) images are accepted. When a single plane image is the input, the output is a processed single plane image. When Z-stacks are the input, the output is a processed Z-stack, and an additional special Z-projection, condensing 3D colocalization data onto a single plane. This Z-projection is primarily designed for use with 3D image data in which the Z-dimension is limited in depth as to include only a few layers of objects of interest, which is often the case in thin physical sections processed for fluorescent labeling. Appropriately sized sections can also be digitally extracted portions of thicker 3D image data. The Z-projection is inserted as a single slice on top of the output Z-stack. The purpose of the Z-projection is to simplify colocalization analysis and reduce the time spent, by permitting inspection of the Z-projection primarily during semi-automatic OBCA quantification, and inspecting the underlying Z-stack only if necessary or desired (see plugin 2 for the quantification process). The special Z-projection is designed to algorithmically remove Z-projection artifacts (Figure 2A). It is important to realize, however, that effective removal of Z-projection artifacts requires that the Z-stack input is generated with sufficient resolution, in particular in the Z-axis (optical slice thickness). See the Supplementary Information section online entitled “ImageJ plugin 1” for specific details on the plugin operation and user interfaces.

