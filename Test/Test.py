from pathlib import Path
import os
import subprocess
import yaml


def Main():
    global runTestCmd
    path = str(Path(__file__).parents[1])
    directory = str(Path(__file__).parent)+"/Tests/"
    # print(str(Path(__file__).parent))
    # exit()

    # runTestCmd = f"java \"-XX:+ShowCodeDetailsInExceptionMessages\" -cp \"{path}\\bin\" \"LungoBrowser.App\" -d -sn \"result/$out\" -l \"$url\" -s $width $height"

    with open(str(Path(__file__).parent)+"/settings.yaml", "r") as stream:
        settings = yaml.safe_load(stream)
        runTestCmd = settings["runUrl"].replace("$path", path)

        f = []
        for (dirpath, dirnames, filenames) in os.walk(directory):
            f.extend(filenames)
            break

        for elm in f:
            RunYamlFile(elm)

    
    # os.system(f"java \"-XX:+ShowCodeDetailsInExceptionMessages\" -cp \"{path}\\bin\" \"LungoBrowser.App\" -d -sn \"result/test.jpg\" -s 800 800");

def RunYamlFile(fileName):
    with open(str(Path(__file__).parent)+f"/Tests/{fileName}", "r") as stream:
        try:
            RunYamlFromFile(yaml.safe_load(stream), fileName.replace(".yaml", ""))
        except yaml.YAMLError as exc:
            print(exc)

def RunYamlFromFile(yaml, name):
    print(yaml)
    size = yaml["size"]
    command = runTestCmd.replace("$url", yaml["url"]).replace("$out", str(Path(__file__).parent)+"/result/"+name+".jpg").replace("$width", str(size["width"])).replace("$height", str(size["height"]))
    try:
        subprocess.run(command, shell=True, check=True)
    except subprocess.CalledProcessError as e:
        print(f"Command execution failed: {e}")

if __name__ == '__main__':
    Main()