import React from 'react';
import logo from './logo.svg';
import './App.css';

class App extends React.Component<{}, {}> {

  onFileUpload(e: any){
    console.log(e.target.files[0]);
    const formData = new FormData();
    formData.append("file", e.target.files[0]);
    const headers = {
      body: formData,
      method: "POST"
    };
    fetch("/file", headers).then(result => {return result.json()}).then(res => console.log(res));
  }

  render(){
    return (
        <div className="App">
          <input type="file" name="file" onChange={this.onFileUpload}/>
        </div>
    );
  }
}

export default App;
