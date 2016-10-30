import os
import sys
import time
import subprocess
import zipfile
import io
try:
    import xml.etree.cElementTree as ET
except:
    import xml.etree.ElementTree as ET

original_apk_filename = ''
apk_project_name = ''
package_name = ''
#This function is used to check whether
#the apk to be repacked is ready or not
def check_original_app():
    global original_apk_filename
    if len(sys.argv) != 2:
        print "orignal apk parameter is missing!"
        return False
    else:
        original_apk_filename = sys.argv[1]
        if os.path.exists('./' + original_apk_filename):
            print original_apk_filename + " does exist"
            return True
        else:
            print original_apk_filename + " doesn't exist"
            return False

#prepare the repacker app project directory
def create_apk_project():
    global original_apk_filename
    global apk_project_name
    apk_project_name = ""
    if original_apk_filename.endswith(".apk"):
        apk_project_name = original_apk_filename[0:len(original_apk_filename) - 4]
    else:
        apk_project_name = original_apk_filename
    print "apk_project_name: " + apk_project_name
    apk_project_name = apk_project_name + "Project"
    if os.path.exists(apk_project_name):
        print "repacker project directory " + apk_project_name + " already exists!!"
        return False
    else:
        print "create new project directory"
        os.mkdir("./" + apk_project_name)
        return True

#Use command line to call apktool to decompile apk resources..
def decompile_original_apk():
    global original_apk_filename
    apktool_pipe = subprocess.Popen("apktool d " + original_apk_filename, shell=True)
    apktool_pipe.wait()

#copy asset, res .etc directory to new created project.
#To support jni, rename lib directory to libs if it exists...
def copy_apk_resouce(apk_path):
    global apk_project_name
    file_list = os.listdir(apk_path)
    assets_exist = False
    jni_exist = False
    #print file_list
    for item in file_list:
        if item == "assets":
            assets_exist = True
        
        if item == "lib":
            jni_exist = True
        item_path = "./" + apk_path + "/" + item
        if os.path.isfile(item_path) and item != "AndroidManifest.xml":
            continue
        elif os.path.isdir(item_path) and item == "smali":
            continue
        elif os.path.isfile(item_path):
            os.system("cp " + item_path + " ./" + apk_project_name + "/" + item)
        elif os.path.isdir(item_path) and item != "lib":
            os.system("cp -r " + item_path + " ./" + apk_project_name + "/" + item)
        else:
            #skip jni lib directory here, we will handle this finally
            continue
    
    if not assets_exist:
        os.mkdir("./" + apk_project_name + "/assets")
    
    if jni_exist:
        if not os.path.exists("./" + apk_project_name + "/libs"):
            os.mkdir("./" + apk_project_name + "/libs")
        os.system("cp -r ./" + apk_path + "/lib/* " + " ./" + apk_project_name + "/libs/")
    
# parse AndroidManifest.xml, finished following jobs:
#1. handle manifest node
#2. get package name
#3. insert shell application and meta-data info
#4. add <uses-sdk /> node ...
def handle_android_manifest():
    xml_file_path = "./" + apk_project_name + "/AndroidManifest.xml"
    if os.path.exists(xml_file_path) != True:
        print "AndroidManifest.xml is missing in " + apk_project_name
        return False
    
    #some init work for xml handle
    android_namespace = "http://schemas.android.com/apk/res/android"
    ET.register_namespace("android", android_namespace)
    android_manifest_tree = ET.ElementTree(file = xml_file_path)
    manifest_root = android_manifest_tree.getroot()
    meta_node3 = ET.Element("uses-permission", {"android:name" : "android.permission.READ_EXTERNAL_STORAGE"})
    meta_node3.tail="\n"
    meta_node4 = ET.Element("uses-permission", {"android:name" : "android.permission.SYSTEM_ALERT_WINDOW"})
    meta_node4.tail="\n"
    manifest_root.append(meta_node3)
    manifest_root.append(meta_node4)

    clean_manifest_root(manifest_root, android_namespace)
    global package_name
    package_name = manifest_root.get("package")
    print "Get original apk's package name: " + package_name
    #get application element node
    for child in manifest_root:
        if child.tag == "application":
            application_node = child
            break
    app_name_key = "{" + android_namespace + "}name"
    app_name_val = application_node.get(app_name_key)
    if app_name_val == None:
        app_name_val = "android.app.Application"
    meta_key1 = app_name_key
    meta_key2 = "{" + android_namespace + "}value"
    meta_node1 = ET.Element("meta-data", {meta_key1 : "APPLICATION_CLASS_NAME", meta_key2 :app_name_val})
    launch_activity_name = get_launch_activity(application_node, android_namespace)
    print "launch activity name: " + launch_activity_name
    meta_node2 = ET.Element("meta-data", {meta_key1 : "LAUNCH_ACTIVITY_NAME", meta_key2 :launch_activity_name})
    Logservice_name=package_name+".Logservice"
    StopService_name=package_name+".StopService"
    Service_node=ET.Element("service",{"android:name":Logservice_name})
    Service1_node=ET.Element("service",{"android:name":StopService_name,"android:process":".StopService"})
    application_node.append(Service1_node)
    application_node.append(Service_node)
    application_node.append(meta_node1)
    application_node.append(meta_node2)
    version_lst = get_sdk_version()
    sdk_config_node = create_sdk_config_node(version_lst, android_namespace)
    if sdk_config_node != None:
        manifest_root.append(sdk_config_node)
    #android_manifest_tree.write(xml_file_path)
    
    #replace/add the application node's android:name attribute
    shell_application_name = package_name + ".ShellApplication"
    application_node.set(app_name_key, shell_application_name)
    android_manifest_tree.write(xml_file_path)
    
def get_sdk_version():
    version_list = []
    min_sdk_version_pipe = os.popen("aapt dump badging " + original_apk_filename + ' |grep "sdkVersion:"')
    min_version_line = min_sdk_version_pipe.read().split(':')[1]
    min_version_line = min_version_line.strip().strip("'")
    if min_version_line == "":
        min_version_line = "-1"
    print "minimum sdk version is " + min_version_line
    version_list.append(min_version_line)
    
    tar_sdk_version_pipe = os.popen("aapt dump badging " + original_apk_filename + ' |grep "targetSdkVersion:"')
    tar_version_line = tar_sdk_version_pipe.read().split(':')[1]
    tar_version_line = tar_version_line.strip().strip("'")
    if tar_version_line == "":
        tar_version_line = "-1"
        
    print "target sdk version is " + tar_version_line
    version_list.append(tar_version_line)
    return version_list

def create_sdk_config_node(version_lst, android_namespace):
    if version_lst[0] == "-1" and version_lst[1] == "-1":
        return None
    sdk_config_node = ET.Element("uses-sdk")
    if version_lst[0] != "-1":
        #should set min sdk version attribute
        min_sdk_key = "{" + android_namespace + "}minSdkVersion"
        sdk_config_node.set(min_sdk_key, version_lst[0])
    
    if version_lst[1] != "-1":
        #should set target sdk version attribute
        tar_sdk_key = "{" + android_namespace + "}targetSdkVersion"
        sdk_config_node.set(tar_sdk_key, version_lst[1])
        
    return sdk_config_node
#clean all attribute of manifest node in AndroidManifest.xml,
#except "package" attribute, I do this to keep the same with eclipse project
#settings......
def clean_manifest_root(manifest_root, key_prefix):
    key_list = []
    for key_item in manifest_root.attrib:
        if key_item == "package":
            continue
        else:
            key_list.append(key_item)
    for key_entry in key_list:
        del manifest_root.attrib[key_entry]
    
    #add 2 attributes for manifest node...
    manifest_root.set("{" + key_prefix + "}versionCode","1")
    manifest_root.set("{" + key_prefix + "}versionName", "1.0")

def get_launch_activity(application, android_namespace):
    launch_activity_node = None
    android_name_str = "{" + android_namespace + "}name"
    for child in application:
        if child.tag != "activity" and child.tag != "activity-alias":
            continue
        for child1 in child:
            if child1.tag == "intent-filter":
                for child2 in child1:
                    if child2.tag == "category":
                        tmp = child2.get(android_name_str)
                        if tmp == "android.intent.category.LAUNCHER":
                            launch_activity_node = child
                            break
    if launch_activity_node.tag == "activity-alias":
        node_key = "{" + android_namespace + "}targetActivity" 
    else:
        node_key = "{" + android_namespace + "}name"
    launch_activity_name = launch_activity_node.get(node_key)
    return launch_activity_name

#Attention! Please copy all source files to a directory named "SourceTemplate" and
#put it in the same directory with app_repacker.py
def handle_source_template():
    #First, prepare src/ directory in new created project directory
    if package_name == '' or apk_project_name == '':
        print "package name or app project name are not initialized correctly!!"
        return False
    source_template_dir = "./SourceTemplate"
    if not os.path.exists(source_template_dir):
        print "Source Template Directory Not Found!!"
        return False
    #all template source files to be copied and modified...
    source_file_list = os.listdir(source_template_dir)
    
    #create corresponding directory in new-created project directory
    package_dir_list = package_name.split(".")
    src_dir_path = "./" + apk_project_name + "/src"
    os.mkdir(src_dir_path)
    for item in package_dir_list:
        src_dir_path = src_dir_path + "/" + item
        os.mkdir(src_dir_path)
    
    #src directory is ready, now copy and modify source template file
    for item in source_file_list:
        src_path = source_template_dir + "/" + item
        des_path = src_dir_path + "/" + item
        apply_source_template(src_path, des_path)
        
    return True

def apply_source_template(src_path, des_path):
    if not os.path.exists(src_path):
        print src_path + "doesn't exist! Please make your source file ready!"
        return
    
    if os.path.exists(des_path):
        print des_path + " already exists in target project, please make your target project clean"
        return
    
    src_file = open(src_path, "r")
    des_file = open(des_path, "w")
    
    #First, add package xxxx to source file
    des_file.write("package " + package_name + ';\n')
    while True:
        source_line = src_file.readline()
        if not source_line:
            break
        des_file.write(source_line)
    
    src_file.close()
    des_file.close()
    
#Attention, please make sure the android sdk tool and ant tool to
#be installed already, and "android" tool in android-sdk-linux/tools
#is already added to env path.
def compile_project():
    update_project_pipe = subprocess.Popen("android update project --name " + apk_project_name + " --target android-24 --path ./" + apk_project_name, shell=True)
    update_project_pipe.wait()
    
    os.chdir("./" + apk_project_name)
    ant_pipe = subprocess.Popen("ant debug", shell=True)
    ant_pipe.wait()
    os.chdir("..")

#copy original dex file to new created project asset/ ...
def prepare_raw_dex():
    zip_file = zipfile.ZipFile(original_apk_filename, "r")
    found_dex = False
    for name in zip_file.namelist():
        if name == "classes.dex":
            found_dex = True
            break
    if not found_dex:
        print "Can't find classes.dex in apk file"
        return False
    dex_content = zip_file.read("classes.dex")
    target_dex_path = "./classes.dex"
    target_dex_file = open(target_dex_path, "w")
    target_dex_file.write(dex_content)
    target_dex_file.close()
    target_jar_path = "./" + apk_project_name + "/assets/real_classes.jar"
    jar_pipe = subprocess.Popen("jar cvf " + target_jar_path + " " + target_dex_path, shell=True)
    jar_pipe.wait()
    jar_pipe = subprocess.Popen("rm -f " + target_dex_path, shell=True)
    jar_pipe.wait()
    return True
if __name__ == "__main__":
    ret = check_original_app()
    if ret == False:
        print "check orignal app failed!"
        exit(0)
    
    ret = create_apk_project()
    if ret == False:
        print "Created repack app project failed"
        exit(0)
    
    decompile_original_apk()
    #sleep 5 seconds to wait for original apk decompilation
    time.sleep(5)
    if original_apk_filename.endswith(".apk"):
        apk_decompile_path = original_apk_filename[0:len(original_apk_filename) - 4]
    else:
        apk_decompile_path = original_apk_filename + ".out"
    
    copy_apk_resouce(apk_decompile_path)
    handle_android_manifest()
    ret = handle_source_template()
    if not ret:
        exit(0)
    
    prepare_raw_dex()
    compile_project()
    rm_decompile_pipe = subprocess.Popen("rm -rf ./" + apk_decompile_path, shell=True)
    rm_decompile_pipe.wait()
    print "repacking finished!"
