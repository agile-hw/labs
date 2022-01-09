# labs
Labs for the Agile Hardware Design course in Jupyter Notebooks. The binder link is available here: https://mybinder.org/v2/gh/agile-hw/labs/HEAD?urlpath=tree

### Local Installation
- Binder will discard any work after 10 minutes of idle time.
- For local development the install script will install JupyterLab to a virtual environment assuming python3 is available and will install the Scala kernel 'Almond'.
- To run the script run via commandline: `./install.sh`.
- Activate the environment via `source chisel_nb_env/bin/activate`.
- Start Jupyter Notebook via `jupyter notebook` then navigate to the provided url in your browser.
- Use the file browser to navigate to the lab.

### Acknowledgements
`resource/chisel_deps.sc` and `binder` configs are borrowed from the Chisel bootcamp: https://github.com/freechipsproject/chisel-bootcamp
