import React from "react";
import {BrowserRouter as Router, Routes, Route} from 'react-router-dom';
import Layout from "./components/layout/Layout";

const DataManagement: React.FC = () => <div>Zarządzanie Plikami</div>
const Users: React.FC = () => <div>Użytkownicy</div>

const App: React.FC = () => {
    return (
        <Router>
            <Layout>
                <Routes>
                    {/*
                    <Route path="/upload" element={<ExcelUpload/>}/>
*/}
                    <Route path="/data" element={<DataManagement/>}/>
                    <Route path="/users" element={<Users/>}/>
{/*
                    <Route path="/" element={<ExcelUpload/>}/>
*/}
                </Routes>
            </Layout>
        </Router>
    );
};

export default App;