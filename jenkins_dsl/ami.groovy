// file: jenkins_dsl/ami.groovy
//

// JDK to use configured in Jenkins global settings
def jdkType = "Slave_Oracle_JDK8"

// Jenkins Slave swarm to use
def swarm = "packer"

// Authentication Token to Trigger builds remotely
def authenticationToken = "IAMTHOMSONREUTERS"

def amis = [  "ami-base": 
                [ "repo": ["https://github.com/ervansetiawan/ami-roles.git", "master"],
                  "name":"base",
                  "ami_profile":"base",
                  "ami_parent":"amazon_linux",
                  "job_tag":"build"
                ],
              "ami-jenkins": 
                [ "repo": ["https://github.com/daltonconley/ami-roles.git", "master"],
                  "name":"jenkins",
                  "ami_profile":"jenkins",
                  "ami_parent":"base",                  
                  "job_tag":"build"
                ],                
              "ami-jenkins-slave": 
                [ "repo": ["https://github.com/daltonconley/ami-roles.git", "master"],
                  "name":"jenkins-slave",
                  "ami_profile":"jenkins-slave",
                  "ami_parent":"base",                  
                  "job_tag":"build"
                ],            
              "ami-bastion": 
                [ "repo": ["https://github.com/daltonconley/ami-roles.git", "master"],
                  "name":"bastion",
                  "ami_profile":"bastion",
                  "ami_parent":"base",                  
                  "job_tag":"build"    
                ],           
              "ami-etcd":
                [ "repo": ["https://github.com/daltonconley/ami-roles.git", "master"],
                  "name":"etcd",
                  "ami_profile":"etcd",
                  "ami_parent":"base",                  
                  "job_tag":"build"
                ]


            ]


amis.values().each { ami ->
def jobname = "ami-" + ami.name
  
  freeStyleJob(jobname) {

    jdk(jdkType)
    label(swarm)

    triggers {
      githubPush()
    }

    configure { project ->
      project / 'authToken' (authenticationToken){}
    }

    steps {
      shell("bin/provision_base_ami")
    }

      
    scm {
      git {
        remote {
          url(ami.repo.get(0))
          branch(ami.repo.get(1))
          credentials('tripscloud Github')
        }
      }
    }

    publishers {
      archiveArtifacts { 
        pattern('AMI-$AMI_PROFILE')
      }
    }


    parameters {
      stringParam("AMI_PROFILE", ami.ami_profile, "Profile to use")
      stringParam("AMI_PARENT", ami.ami_parent, "Parent profile to use. Also will take an AMI ID.")
      stringParam("JOB_TAG", ami.job_tag, "Job Tag to use")
      choiceParam("RELEASE", ["snapshot", "scratch", "stable"], "Testing or release version.")
      choiceParam("AMI_PARENT_RELEASE", ["stable", "snapshot", "scratch"], "Testing or release version.")
    }
  }
}


