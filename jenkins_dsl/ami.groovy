// file: jenkins_dsl/ami.groovy
//

// Jenkins Slave swarm to use
def swarm = "packer"

def amis = [  "ami-base": 
                [ "repo": ["https://git.sami.int.thomsonreuters.com/dalton.conley/ami-roles.git", "master"],
                  "name":"base",
                  "ami_profile":"base",
                  "ami_parent":"amazon_linux",
                  "job_tag":"build"
                ],
              "ami-jenkins": 
                [ "repo": ["https://git.sami.int.thomsonreuters.com/dalton.conley/ami-roles.git", "master"],
                  "name":"jenkins",
                  "ami_profile":"jenkins",
                  "ami_parent":"base",                  
                  "job_tag":"build"
                ],                
              "ami-jenkins-slave": 
                [ "repo": ["https://git.sami.int.thomsonreuters.com/dalton.conley/ami-roles.git", "master"],
                  "name":"jenkins-slave",
                  "ami_profile":"jenkins-slave",
                  "ami_parent":"base",                  
                  "job_tag":"build"
                ],            
              "ami-bastion": 
                [ "repo": ["https://git.sami.int.thomsonreuters.com/dalton.conley/ami-roles.git", "master"],
                  "name":"bastion",
                  "ami_profile":"bastion",
                  "ami_parent":"base",                  
                  "job_tag":"build"    
                ],           
              "ami-etcd":
                [ "repo": ["https://git.sami.int.thomsonreuters.com/dalton.conley/ami-roles.git", "master"],
                  "name":"etcd",
                  "ami_profile":"etcd",
                  "ami_parent":"base",                  
                  "job_tag":"build"
                ],
              "ami-elasticsearch": 
                [ "repo": ["https://git.sami.int.thomsonreuters.com/dalton.conley/ami-roles.git", "master"],
                  "name":"elasticsearch",
                  "ami_profile":"elasticsearch",
                  "ami_parent":"base",                  
                  "job_tag":"build"
                ]
            ]


amis.values().each { ami ->
def jobname = "ami-" + ami.name
  
  freeStyleJob(jobname) {
    label(swarm)

    triggers {
      githubPush()
    }

    steps {
      shell("bin/provision_base_ami")
    }

      
    scm {
      git {
        remote {
          url(ami.repo.get(0))
          branch(ami.repo.get(1))
          credentials('635eba32-69cb-4eb7-a8e9-0ae209cf3706')
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


