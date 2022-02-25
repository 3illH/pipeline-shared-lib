package org.shared.utils

class Checkout implements Serializable {
  def steps
  Checkout(steps) {this.steps = steps}
  def checkout(args) {
    steps.checkout([$class: 'GitSCM', branches: [[name: 'master']], extensions: [], userRemoteConfigs: [[url: args]]])
  }
}